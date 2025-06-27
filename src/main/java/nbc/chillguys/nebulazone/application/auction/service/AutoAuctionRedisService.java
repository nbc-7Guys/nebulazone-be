package nbc.chillguys.nebulazone.application.auction.service;

import static nbc.chillguys.nebulazone.application.auction.consts.AuctionConst.*;
import static nbc.chillguys.nebulazone.application.bid.consts.BidConst.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nbc.chillguys.nebulazone.domain.auction.entity.Auction;
import nbc.chillguys.nebulazone.domain.auction.service.AutoAuctionDomainService;
import nbc.chillguys.nebulazone.domain.bid.entity.BidStatus;
import nbc.chillguys.nebulazone.domain.bid.service.BidDomainService;
import nbc.chillguys.nebulazone.domain.product.entity.Product;
import nbc.chillguys.nebulazone.domain.product.service.ProductDomainService;
import nbc.chillguys.nebulazone.domain.transaction.dto.TransactionCreateCommand;
import nbc.chillguys.nebulazone.domain.transaction.entity.UserType;
import nbc.chillguys.nebulazone.domain.transaction.service.TransactionDomainService;
import nbc.chillguys.nebulazone.domain.user.entity.User;
import nbc.chillguys.nebulazone.domain.user.service.UserDomainService;
import nbc.chillguys.nebulazone.infra.redis.vo.AuctionVo;
import nbc.chillguys.nebulazone.infra.redis.vo.BidVo;

@Slf4j
@Service
@RequiredArgsConstructor
public class AutoAuctionRedisService {

	private final RedisTemplate<String, Object> redisTemplate;
	private final ObjectMapper objectMapper;
	private final RedissonClient redissonClient;

	private final AutoAuctionDomainService autoAuctionDomainService;
	private final ProductDomainService productDomainService;
	private final BidDomainService bidDomainService;
	private final UserDomainService userDomainService;
	private final TransactionDomainService transactionDomainService;

	/**
	 * 자동 경매 종료<br>
	 * 유찰 - 입찰 내역이 없음, logging만 수행<br>
	 * 입찰 - 판매자 포인트 증가, 낙찰안된 입찰자 포인트 반환, 상품 및 경매 상태 변경, 거래내역 생성
	 * @param auctionId 종료할 경매 id
	 * @author 전나겸
	 */
	@Transactional
	public void processAuctionEnding(Long auctionId) {
		RLock autoAuctionLock = redissonClient.getLock(AUCTION_LOCK_ENDING_PREFIX + auctionId);

		try {
			if (!autoAuctionLock.tryLock()) {
				return;
			}

			Map<Object, Object> auctionMap = redisTemplate.opsForHash().entries(AUCTION_PREFIX + auctionId);
			AuctionVo auctionVo = objectMapper.convertValue(auctionMap, AuctionVo.class);

			Set<Object> objects = redisTemplate.opsForZSet().range(BID_PREFIX + auctionId, 0, -1);
			Auction auction = autoAuctionDomainService.autoEndAuction(auctionId, auctionVo.getCurrentPrice());

			if (auction == null) {
				log.warn("자동 낙찰 대상이 없으므로 자동 낙찰 프로세스 자동 종료. 자동 종료 시도한 경매 id: {}", auctionId);
				cleanUpRedisAuctionAndBid(auctionId);
				return;
			}

			if (auction.isWon()) {
				Product wonAuctionProduct = auction.getProduct();
				wonAuctionProduct.purchase();

				User seller = wonAuctionProduct.getSeller();
				seller.addPoint(auction.getCurrentPrice());

				List<BidVo> bidVoList = Optional.ofNullable(objects)
					.orElse(Set.of())
					.stream()
					.map(bid -> objectMapper.convertValue(bid, BidVo.class))
					.peek(bidVo -> {
						if (bidVo.getBidPrice().equals(auction.getCurrentPrice())) {
							bidVo.wonBid();
						}
					})
					.toList();

				List<Long> bidUserIds = bidVoList.stream().map(BidVo::getBidUserId).distinct().toList();

				List<User> bidUsers = userDomainService.findActiveUserByIds(bidUserIds);

				Map<Long, User> userMap = bidUsers.stream().collect(Collectors.toMap(User::getId, user -> user));

				bidVoList.stream().filter(bidVo -> userMap.containsKey(bidVo.getBidUserId())).forEach(bidVo -> {
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

			}

			cleanUpRedisAuctionAndBid(auctionId);

		} finally {
			autoAuctionLock.unlock();
		}

	}

	private void cleanUpRedisAuctionAndBid(Long auctionId) {
		redisTemplate.delete(AUCTION_ENDING_PREFIX);
		redisTemplate.delete(AUCTION_PREFIX + auctionId);
		redisTemplate.delete(BID_PREFIX + auctionId);
	}
}
