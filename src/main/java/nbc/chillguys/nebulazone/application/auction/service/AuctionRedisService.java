package nbc.chillguys.nebulazone.application.auction.service;

import static nbc.chillguys.nebulazone.application.auction.consts.AuctionConst.*;
import static nbc.chillguys.nebulazone.application.bid.consts.BidConst.*;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.application.auction.dto.request.ManualEndAuctionRequest;
import nbc.chillguys.nebulazone.application.auction.dto.response.ManualEndAuctionResponse;
import nbc.chillguys.nebulazone.domain.auction.entity.Auction;
import nbc.chillguys.nebulazone.domain.auction.exception.AuctionErrorCode;
import nbc.chillguys.nebulazone.domain.auction.exception.AuctionException;
import nbc.chillguys.nebulazone.domain.auction.service.AuctionDomainService;
import nbc.chillguys.nebulazone.domain.bid.entity.BidStatus;
import nbc.chillguys.nebulazone.domain.bid.exception.BidErrorCode;
import nbc.chillguys.nebulazone.domain.bid.exception.BidException;
import nbc.chillguys.nebulazone.domain.bid.service.BidDomainService;
import nbc.chillguys.nebulazone.domain.product.entity.Product;
import nbc.chillguys.nebulazone.domain.product.entity.ProductEndTime;
import nbc.chillguys.nebulazone.domain.transaction.dto.TransactionCreateCommand;
import nbc.chillguys.nebulazone.domain.transaction.entity.UserType;
import nbc.chillguys.nebulazone.domain.transaction.service.TransactionDomainService;
import nbc.chillguys.nebulazone.domain.user.entity.User;
import nbc.chillguys.nebulazone.domain.user.service.UserDomainService;
import nbc.chillguys.nebulazone.infra.redis.dto.CreateRedisAuctionDto;
import nbc.chillguys.nebulazone.infra.redis.vo.AuctionVo;
import nbc.chillguys.nebulazone.infra.redis.vo.BidVo;

@Service
@RequiredArgsConstructor
public class AuctionRedisService {

	private final RedisTemplate<String, Object> redisTemplate;
	private final ObjectMapper objectMapper;
	private final RedissonClient redissonClient;
	private final AuctionDomainService auctionDomainService;
	private final UserDomainService userDomainService;
	private final BidDomainService bidDomainService;
	private final TransactionDomainService transactionDomainService;

	/**
	 * redis 경매 생성<br>
	 * hash로 auctionVo 저장, ZSet으로 경매 종료 순서를 관리
	 * @param redisAuctionDto 경매 생성을 위한 요청값
	 * @author 전나겸
	 */
	public void createAuction(CreateRedisAuctionDto redisAuctionDto) {

		Product product = redisAuctionDto.product();
		Auction auction = redisAuctionDto.auction();
		User user = redisAuctionDto.user();
		ProductEndTime productEndTime = redisAuctionDto.ProductEndTime();

		String auctionKey = AUCTION_PREFIX + auction.getId();
		AuctionVo auctionVo = AuctionVo.of(product, auction, user);

		Map<String, Object> auctionVoMap = objectMapper.convertValue(auctionVo, new TypeReference<>() {
		});
		redisTemplate.opsForHash().putAll(auctionKey, auctionVoMap);

		long endTimestamp = System.currentTimeMillis() / 1000 + productEndTime.getSeconds();
		redisTemplate.opsForZSet().add(AUCTION_ENDING_PREFIX, auction.getId(), endTimestamp);

	}

	/**
	 * 특정 경매 조회<br>
	 * AuctionVo 반환
	 * @param auctionId 조회할 경매 id
	 * @return AuctionVo
	 * @author 전나겸
	 */
	public AuctionVo getAuctionVo(Long auctionId) {
		Map<Object, Object> auctionMap = redisTemplate.opsForHash()
			.entries(AUCTION_PREFIX + auctionId);

		if (auctionMap.isEmpty()) {
			throw new AuctionException(AuctionErrorCode.AUCTION_NOT_FOUND);
		}

		return objectMapper.convertValue(auctionMap, AuctionVo.class);
	}

	/**
	 * 특정 경매의 입찰 최고가 갱신
	 * @param auctionId 대상 경매 id
	 * @param bidPrice 갱신할 입찰가 (null 가능 - 입찰이 모두 취소된 경우)
	 * @author 전나겸
	 */
	public void updateAuctionCurrentPrice(Long auctionId, Long bidPrice) {
		String auctionKey = "auction:" + auctionId;

		redisTemplate.opsForHash().put(auctionKey, "currentPrice", bidPrice);

	}

	@Transactional
	public ManualEndAuctionResponse manualEndAuction(Long auctionId, User loginUser, ManualEndAuctionRequest request) {
		RLock auctionEndingLock = redissonClient.getLock(AUCTION_LOCK_ENDING_PREFIX + auctionId);

		try {
			if (!auctionEndingLock.tryLock()) {
				throw new AuctionException(AuctionErrorCode.AUCTION_PROCESSING_BUSY);
			}

			Map<Object, Object> auctionMap = redisTemplate.opsForHash().entries(AUCTION_PREFIX + auctionId);
			AuctionVo auctionVo = objectMapper.convertValue(auctionMap, AuctionVo.class);

			auctionVo.validNotAuctionOwner(loginUser);
			auctionVo.validMismatchBidPrice(request.bidPrice());
			auctionVo.validAuctionNotClosed();
			auctionVo.validWonAuction();

			Auction auction = auctionDomainService.manualEndAuction(auctionId, request.bidPrice());
			Product wonAuctionProduct = auction.getProduct();
			wonAuctionProduct.purchase();

			User seller = wonAuctionProduct.getSeller();
			seller.addPoint(auction.getCurrentPrice());

			Set<Object> objects = redisTemplate.opsForZSet().range(BID_PREFIX + auctionId, 0, -1);

			List<BidVo> bidVoList = Optional.ofNullable(objects)
				.orElse(Set.of())
				.stream()
				.map(bid -> objectMapper.convertValue(bid, BidVo.class))
				.peek(bidVo -> {
					if (bidVo.getBidPrice().equals(auction.getCurrentPrice())
						&& bidVo.getBidStatus().equals(BidStatus.BID.name())) {
						bidVo.wonBid();
					}
				})
				.toList();

			BidVo wonBidVo = bidVoList.stream()
				.filter(bidVo -> BidStatus.BID.name().equals(bidVo.getBidStatus()))
				.max(Comparator.comparing(BidVo::getBidPrice))
				.orElseThrow(() -> new BidException(BidErrorCode.BID_NOT_FOUND));

			wonBidVo.validNotBidOwner(request.bidUserId());

			List<Long> bidUserIds = bidVoList
				.stream()
				.map(BidVo::getBidUserId)
				.distinct()
				.toList();

			List<User> bidUsers = userDomainService.findActiveUserByIds(bidUserIds);

			Map<Long, User> userMap = bidUsers.stream().collect(Collectors.toMap(User::getId, user -> user));
			bidVoList.stream()
				.filter(bidVo -> userMap.containsKey(bidVo.getBidUserId()))
				.forEach(bidVo -> {

					User bidUser = userMap.get(bidVo.getBidUserId());

					if (BidStatus.WON.name().equals(bidVo.getBidStatus())) {
						TransactionCreateCommand buyerTxCreateCommand = TransactionCreateCommand.of(bidUser,
							UserType.BUYER, wonAuctionProduct, wonAuctionProduct.getTxMethod().name(),
							auction.getCurrentPrice());
						transactionDomainService.createTransaction(buyerTxCreateCommand);

					} else if (BidStatus.BID.name().equals(bidVo.getBidStatus())) {
						bidUser.addPoint(bidVo.getBidPrice());
					}
				});

			bidDomainService.createAllBid(auction, bidVoList, userMap);

			TransactionCreateCommand sellerTxCreateCommand = TransactionCreateCommand.of(seller, UserType.SELLER,
				wonAuctionProduct, wonAuctionProduct.getTxMethod().name(), auction.getCurrentPrice());

			transactionDomainService.createTransaction(sellerTxCreateCommand);

			redisTemplate.opsForZSet().remove(AUCTION_ENDING_PREFIX, auctionId);
			redisTemplate.delete(AUCTION_PREFIX + auctionId);
			redisTemplate.delete(BID_PREFIX + auctionId);

			return ManualEndAuctionResponse.of(auction, wonBidVo, wonAuctionProduct);

		} finally {
			auctionEndingLock.unlock();
		}
	}

	// todo : 내 경매 삭제

	// todo : 정렬 기반 경매 조회

	// todo : 내 경매 내역 조회

	// todo : 경매 상세 조회

}
