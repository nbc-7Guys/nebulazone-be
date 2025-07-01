package nbc.chillguys.nebulazone.infra.redis.service;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nbc.chillguys.nebulazone.domain.auction.entity.Auction;
import nbc.chillguys.nebulazone.domain.auction.service.AuctionDomainService;
import nbc.chillguys.nebulazone.domain.bid.entity.Bid;
import nbc.chillguys.nebulazone.domain.bid.service.BidDomainService;
import nbc.chillguys.nebulazone.domain.user.entity.User;
import nbc.chillguys.nebulazone.domain.user.service.UserDomainService;
import nbc.chillguys.nebulazone.infra.redis.constant.AuctionConstants;
import nbc.chillguys.nebulazone.infra.redis.constant.BidConstants;
import nbc.chillguys.nebulazone.infra.redis.lock.DistributedLock;
import nbc.chillguys.nebulazone.infra.redis.vo.AuctionVo;
import nbc.chillguys.nebulazone.infra.redis.vo.BidVo;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisBackupService {

	private final RedisTemplate<String, Object> redisTemplate;
	private final ObjectMapper objectMapper;

	private final UserDomainService userDomainService;
	private final AuctionDomainService auctionDomainService;
	private final BidDomainService bidDomainService;

	@EventListener
	@DistributedLock(key = "'backup:shutdown'", waitTime = 30, leaseTime = 60)
	public void onApplicationShutdown(ContextClosedEvent event) {
		log.info("애플리케이션 종료 감지 - Redis 데이터 백업 시작");

		try {
			backupActiveAuctionsAndBids();
			log.info("Redis 데이터 백업 완료");
		} catch (Exception e) {
			log.error("Redis 데이터 백업 실패", e);
		}
	}

	@EventListener
	@Order(1)
	@DistributedLock(key = "'recovery:startup'", waitTime = 60, leaseTime = 300)
	public void onApplicationReady(ApplicationReadyEvent event) {
		log.info("애플리케이션 시작 - Redis 데이터 복구 시작");

		try {
			recoverActiveAuctionsAndBids();
			log.info("Redis 데이터 복구 완료");
		} catch (Exception e) {
			log.error("Redis 데이터 복구 실패", e);
		}
	}

	private void backupActiveAuctionsAndBids() {
		Set<Object> activeAuctionIds = redisTemplate.opsForZSet()
			.range(AuctionConstants.AUCTION_ENDING_PREFIX, 0, -1);

		if (activeAuctionIds == null || activeAuctionIds.isEmpty()) {
			log.info("백업할 활성 경매가 없습니다.");
			return;
		}

		int backupCount = 0;

		for (Object auctionIdObj : activeAuctionIds) {
			Long auctionId = ((Number)auctionIdObj).longValue();

			try {
				backupSingleAuction(auctionId);
				backupSingleAuctionBids(auctionId);
				backupCount++;
			} catch (Exception e) {
				log.error("경매 {} 백업 실패", auctionId, e);
			}
		}

		log.info("Redis 데이터 백업 완료: {} 개 경매", backupCount);
	}

	private void recoverActiveAuctionsAndBids() {
		List<Auction> activeAuctions = auctionDomainService.findActiveAuctionsForRecovery();

		if (activeAuctions.isEmpty()) {
			log.info("복구할 활성 경매가 없습니다.");
			return;
		}

		int recoveryCount = 0;

		for (Auction auction : activeAuctions) {
			try {
				boolean auctionRecovered = recoverSingleAuction(auction);
				boolean bidRecovered = recoverSingleAuctionBids(auction.getId());

				if (auctionRecovered || bidRecovered) { // 🔥 실제로 복구된 경우만
					recoveryCount++;
				}
			} catch (Exception e) {
				log.error("경매 {} 복구 실패", auction.getId(), e);
			}
		}

		log.info("Redis 데이터 복구 완료: {} 개 경매", recoveryCount);
	}

	private void backupSingleAuction(Long auctionId) {
		Map<Object, Object> auctionData = redisTemplate.opsForHash()
			.entries(AuctionConstants.AUCTION_PREFIX + auctionId);

		if (!auctionData.isEmpty()) {
			AuctionVo auctionVo = objectMapper.convertValue(auctionData, AuctionVo.class);
			auctionDomainService.updateCurrentPriceForBackup(auctionId, auctionVo.getCurrentPrice());
		}
	}

	private void backupSingleAuctionBids(Long auctionId) {
		Set<Object> bidData = redisTemplate.opsForZSet()
			.range(BidConstants.BID_PREFIX + auctionId, 0, -1);

		if (bidData != null && !bidData.isEmpty()) {
			List<BidVo> bidVoList = bidData.stream()
				.map(o -> objectMapper.convertValue(o, BidVo.class))
				.toList();

			if (!bidVoList.isEmpty()) {
				List<Bid> existingBids = bidDomainService.findActiveBidsForRecovery(auctionId);

				Auction auction = auctionDomainService.findByAuctionId(auctionId);

				List<Long> userIds = bidVoList.stream()
					.map(BidVo::getBidUserId)
					.distinct()
					.toList();

				List<User> users = userDomainService.findActiveUserByIds(userIds);
				Map<Long, User> userMap = users.stream()
					.collect(Collectors.toMap(User::getId, user -> user));

				List<Bid> newBids = new ArrayList<>();

				for (BidVo bidVo : bidVoList) {
					boolean exists = existingBids.stream()
						.anyMatch(bid -> bid.getBidUserId().equals(bidVo.getBidUserId())
							&& bid.getPrice().equals(bidVo.getBidPrice())
							&& bid.getStatus().name().equals(bidVo.getBidStatus()));

					if (!exists) {
						User user = userMap.get(bidVo.getBidUserId());
						if (user != null) {
							Bid newBid = Bid.builder()
								.auction(auction)
								.user(user)
								.price(bidVo.getBidPrice())
								.status(bidVo.getBidStatus())
								.build();
							newBids.add(newBid);
						}
					}
				}

				if (!newBids.isEmpty()) {
					bidDomainService.saveAllBids(newBids);
				}
			}
		}
	}

	private boolean recoverSingleAuction(Auction auction) {
		Long auctionId = auction.getId();

		boolean exists = redisTemplate.hasKey(AuctionConstants.AUCTION_PREFIX + auctionId);

		if (exists) {
			log.debug("경매 {}는 이미 Redis에 존재합니다.", auctionId);
			return false;
		}

		AuctionVo auctionVo = AuctionVo.fromAuction(auction);
		Map<String, Object> auctionData = objectMapper.convertValue(auctionVo, new TypeReference<>() {
		});

		redisTemplate.opsForHash().putAll(AuctionConstants.AUCTION_PREFIX + auctionId, auctionData);

		long endTimestamp = auction.getEndTime().atZone(ZoneId.of("Asia/Seoul")).toEpochSecond();
		redisTemplate.opsForZSet().add(AuctionConstants.AUCTION_ENDING_PREFIX, auctionId, endTimestamp);

		log.debug("경매 {} Redis 복구 완료", auctionId);
		return true;
	}

	private boolean recoverSingleAuctionBids(Long auctionId) {

		boolean bidExists = redisTemplate.hasKey(BidConstants.BID_PREFIX + auctionId);

		if (bidExists) {
			log.debug("경매 {} 입찰 데이터는 이미 Redis에 존재합니다.", auctionId);
			return false;
		}

		List<Bid> activeBids = bidDomainService.findActiveBidsForRecovery(auctionId);

		if (activeBids.isEmpty()) {
			return false;
		}

		for (Bid bid : activeBids) {
			BidVo bidVo = BidVo.fromBid(bid);
			redisTemplate.opsForZSet().add(BidConstants.BID_PREFIX + auctionId, bidVo, bid.getPrice());
		}

		log.debug("경매 {} 입찰 데이터 복구 완료: {} 건", auctionId, activeBids.size());

		return true;
	}
}
