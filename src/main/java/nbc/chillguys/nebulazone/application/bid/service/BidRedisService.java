package nbc.chillguys.nebulazone.application.bid.service;

import java.util.Optional;
import java.util.Set;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.application.auction.service.AuctionRedisService;
import nbc.chillguys.nebulazone.application.bid.dto.response.CreateBidResponse;
import nbc.chillguys.nebulazone.application.bid.dto.response.DeleteBidResponse;
import nbc.chillguys.nebulazone.domain.bid.entity.BidStatus;
import nbc.chillguys.nebulazone.domain.bid.exception.BidErrorCode;
import nbc.chillguys.nebulazone.domain.bid.exception.BidException;
import nbc.chillguys.nebulazone.domain.user.entity.User;
import nbc.chillguys.nebulazone.infra.redis.vo.AuctionVo;
import nbc.chillguys.nebulazone.infra.redis.vo.BidVo;

@Service
@RequiredArgsConstructor
public class BidRedisService {

	private static final String BID_PREFIX = "bid:auction:";
	private static final String BID_LOCK_PREFIX = "bid:lock:auction:";
	private static final String BID_DELETE_LOCK_PREFIX = "bid:deleteLock:";

	private final RedisTemplate<String, Object> redisTemplate;
	private final ObjectMapper objectMapper;
	private final RedissonClient redissonClient;
	private final AuctionRedisService auctionRedisService;

	/**
	 * redis 경매 입찰<br>
	 * Redisson RLock을 사용하여 수평 확장시 동시성 문제를 방지
	 * @param auctionId 입찰 대상 경매 id
	 * @param user 로그인 유저(입찰 유저)
	 * @param bidPrice 입찰가
	 * @return CreateBidResponse
	 * @author 전나겸
	 */
	public CreateBidResponse createBid(Long auctionId, User user, Long bidPrice) {

		RLock bidLock = redissonClient.getLock(BID_LOCK_PREFIX + auctionId + ":" + user.getId());

		acquireLockOrThrow(bidLock);

		try {
			AuctionVo auctionVo = auctionRedisService.getAuctionVo(auctionId);

			auctionVo.validateAuctionNotClosed();
			auctionVo.validateWonAuction();
			auctionVo.validateNotAuctionOwner(user.getId());
			auctionVo.validateMinimumBidPrice(bidPrice);

			BidVo bidVo = BidVo.of(auctionId, user, bidPrice);

			String bidKey = BID_PREFIX + auctionId;
			redisTemplate.opsForZSet().add(bidKey, bidVo, bidPrice);

			auctionRedisService.updateAuctionCurrentPrice(auctionId, bidPrice);

			return CreateBidResponse.from(bidVo);

		} finally {
			bidLock.unlock();
		}

	}

	// todo - 내 입찰 전체 조회 redis, rdb 각각 조회해서 한방에 응답해주자 그냥

	// todo - 특정 경매의 입찰 전체 조회, redis, rdb 각각 조회해서 한방에 응답

	/**
	 * redis 경매 입찰 취소<br>
	 * Redisson RLock을 사용하여 동시성 제어
	 * @param user 로그인 유저(입찰 취소 요청자)
	 * @param auctionId 입찰 취소 대상 경매 id
	 * @param bidPrice 취소할 입찰 가격
	 * @return DeleteBidResponse
	 * @author 전나겸
	 */
	public DeleteBidResponse statusBid(User user, Long auctionId, Long bidPrice) {
		RLock bidLock = redissonClient.getLock(BID_DELETE_LOCK_PREFIX + auctionId);

		acquireLockOrThrow(bidLock);

		try {
			AuctionVo auctionVo = auctionRedisService.getAuctionVo(auctionId);

			auctionVo.validateAuctionNotClosed();
			auctionVo.validateWonAuction();
			auctionVo.validateBidCancelBefore30Minutes();

			String bidKey = BID_PREFIX + auctionId;
			Set<Object> objectBidVo = redisTemplate.opsForZSet().rangeByScore(bidKey, bidPrice, bidPrice);

			BidVo findBidVo = Optional.ofNullable(objectBidVo)
				.orElse(Set.of())
				.stream()
				.map(o -> objectMapper.convertValue(o, BidVo.class))
				.findFirst()
				.orElseThrow(() -> new BidException(BidErrorCode.BID_NOT_FOUND));

			findBidVo.validateNotBidOwner(user.getId());
			findBidVo.validateBidStatusIsCancel();
			findBidVo.validateBidStatusIsWon();
			findBidVo.validateAuctionMismatch(auctionId);

			redisTemplate.opsForZSet().remove(bidKey, findBidVo);
			findBidVo.cancelBid();
			redisTemplate.opsForZSet().add(bidKey, findBidVo, bidPrice);

			Set<Object> allBids = redisTemplate.opsForZSet().reverseRange(bidKey, 0, -1);

			Long newCurrentPrice = Optional.ofNullable(allBids)
				.orElse(Set.of())
				.stream()
				.map(o -> objectMapper.convertValue(o, BidVo.class))
				.filter(bidVo -> BidStatus.BID.name().equals(bidVo.getBidStatus()))
				.findFirst()
				.map(BidVo::getBidPrice)
				.orElse(0L);

			auctionRedisService.updateAuctionCurrentPrice(auctionId, newCurrentPrice);

			return DeleteBidResponse.from(findBidVo);

		} finally {
			bidLock.unlock();
		}
	}

	private void acquireLockOrThrow(RLock bidLock) {
		if (!bidLock.tryLock()) {
			throw new BidException(BidErrorCode.BID_PROCESSING_BUSY);
		}
	}
}
