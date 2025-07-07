package nbc.chillguys.nebulazone.application.auction.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nbc.chillguys.nebulazone.application.auction.dto.response.EndAuctionResponse;
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
import nbc.chillguys.nebulazone.infra.redis.constant.AuctionConstants;
import nbc.chillguys.nebulazone.infra.redis.constant.BidConstants;
import nbc.chillguys.nebulazone.infra.redis.lock.DistributedLock;
import nbc.chillguys.nebulazone.infra.redis.publisher.RedisMessagePublisher;
import nbc.chillguys.nebulazone.infra.redis.service.UserCacheService;
import nbc.chillguys.nebulazone.infra.redis.vo.AuctionVo;
import nbc.chillguys.nebulazone.infra.redis.vo.BidVo;

@Slf4j
@Service
@RequiredArgsConstructor
public class AutoAuctionRedisService {

	private final RedisTemplate<String, Object> redisTemplate;
	private final ObjectMapper objectMapper;

	private final AutoAuctionDomainService autoAuctionDomainService;
	private final BidDomainService bidDomainService;
	private final UserDomainService userDomainService;
	private final TransactionDomainService transactionDomainService;
	private final ProductDomainService productDomainService;
	private final UserCacheService userCacheService;
	private final RedisMessagePublisher redisMessagePublisher;

	/**
	 * 자동 경매 종료<br>
	 * 유찰 - 입찰 내역이 없음, logging만 수행<br>
	 * 입찰 - 판매자 포인트 증가, 낙찰안된 입찰자 포인트 반환, 상품 및 경매 상태 변경, 거래내역 생성
	 *
	 * @param auctionId 종료할 경매 id
	 * @author 전나겸
	 */
	@Transactional
	@DistributedLock(key = "'auction:ending:lock:' + #auctionId")
	public void processAuctionEnding(Long auctionId) {

		Map<Object, Object> auctionMap = redisTemplate.opsForHash()
			.entries(AuctionConstants.AUCTION_PREFIX + auctionId);

		if (auctionMap.isEmpty()) {
			log.warn("redis에 없는 데이터가 자동종료 되었음. 자동 종료 시도한 경매 id: {}", auctionId);
			return;
		}

		AuctionVo auctionVo = objectMapper.convertValue(auctionMap, AuctionVo.class);
		Auction auction = autoAuctionDomainService.autoEndAuction(auctionId, auctionVo.getCurrentPrice());

		Set<Object> objects = redisTemplate.opsForZSet().range(BidConstants.BID_PREFIX + auctionId, 0, -1);

		if (auction == null) {
			log.warn("자동 낙찰 대상이 없으므로 자동 낙찰 프로세스 자동 종료. 자동 종료 시도한 경매 id: {}", auctionId);
			cleanUpRedisAuctionAndBid(auctionId);
			return;
		}

		Product wonAuctionProduct = auction.getProduct();

		if (auction.isWon()) {
			wonAuctionProduct.purchase();

			try {
				productDomainService.markProductAsPurchased(wonAuctionProduct.getId());
			} catch (Exception e) {
				log.info("자동 낙찰 완료, ES에 판매 완료로 변경 중 에러발생, productId: {}", wonAuctionProduct.getId(), e);
			}

			User seller = wonAuctionProduct.getSeller();
			seller.plusPoint(auction.getCurrentPrice());

			List<Long> userIdsToInvalidate = new ArrayList<>();
			userIdsToInvalidate.add(seller.getId());

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

			List<Long> bidUserIds = bidVoList.stream()
				.map(BidVo::getBidUserId)
				.distinct()
				.toList();

			List<User> bidUsers = userDomainService.findActiveUserByIds(bidUserIds);

			Map<Long, User> userMap = bidUsers.stream()
				.collect(Collectors.toMap(User::getId, user -> user));

			bidVoList.stream()
				.filter(bidVo -> userMap.containsKey(bidVo.getBidUserId()))
				.forEach(bidVo -> {

					User bidUser = userMap.get(bidVo.getBidUserId());

					if (BidStatus.WON.name().equals(bidVo.getBidStatus())) {
						TransactionCreateCommand buyerTxCreateCommand = TransactionCreateCommand.of(bidUser,
							UserType.BUYER, wonAuctionProduct, wonAuctionProduct.getTxMethod().name(),
							auction.getCurrentPrice(), LocalDateTime.now());

						transactionDomainService.createTransaction(buyerTxCreateCommand);

					} else if (BidStatus.BID.name().equals(bidVo.getBidStatus())) {
						bidUser.plusPoint(bidVo.getBidPrice());
						userIdsToInvalidate.add(bidUser.getId());
					}
				});

			bidDomainService.createAllBid(auction, bidVoList, userMap);

			TransactionCreateCommand sellerTxCreateCommand = TransactionCreateCommand.of(seller, UserType.SELLER,
				wonAuctionProduct, wonAuctionProduct.getTxMethod().name(),
				auction.getCurrentPrice(), LocalDateTime.now());

			transactionDomainService.createTransaction(sellerTxCreateCommand);

			for (Long userId : userIdsToInvalidate) {
				userCacheService.deleteUserById(userId);
			}

			try {
				BidVo wonBidVo = bidVoList.stream()
					.filter(bidVo -> BidStatus.WON.name().equals(bidVo.getBidStatus()))
					.findFirst()
					.orElse(null);

				if (wonBidVo != null) {
					EndAuctionResponse response = EndAuctionResponse.of(auction, wonBidVo, wonAuctionProduct);
					redisMessagePublisher.publishAuctionUpdate(auctionId, "won", response);

					redisMessagePublisher.publishAuctionUpdate(auctionId, "bid", response);
					log.info("자동 낙찰 입찰 상태 업데이트 WebSocket 메시지 발행 성공 - auctionId: {}", auctionId);
				}

			} catch (Exception e) {
				log.error("자동 낙찰 WebSocket 브로드캐스트 실패 - auctionId: {}", auctionId, e);
			}

		} else {
			try {
				EndAuctionResponse response = EndAuctionResponse.of(auction, null, wonAuctionProduct);
				redisMessagePublisher.publishAuctionUpdate(auctionId, "failed", response);
				redisMessagePublisher.publishAuctionUpdate(auctionId, "bid", response);
				log.info("유찰 입찰 상태 업데이트 WebSocket 메시지 발행 성공 - auctionId: {}", auctionId);
			} catch (Exception e) {
				log.error("유찰 WebSocket 브로드캐스트 실패 - auctionId: {}", auctionId, e);
			}

			try {
				productDomainService.deleteProductFromEs(wonAuctionProduct.getId());
			} catch (Exception e) {
				log.info("자동 낙찰(유찰) 완료, ES 삭제 중 에러발생, productId: {}", wonAuctionProduct.getId(), e);
			}

		}

		cleanUpRedisAuctionAndBid(auctionId);

	}

	private void cleanUpRedisAuctionAndBid(Long auctionId) {
		redisTemplate.opsForZSet().remove(AuctionConstants.AUCTION_ENDING_PREFIX, auctionId);
		redisTemplate.delete(AuctionConstants.AUCTION_PREFIX + auctionId);
		redisTemplate.delete(BidConstants.BID_PREFIX + auctionId);
	}
}
