package nbc.chillguys.nebulazone.application.bid.service;

import static nbc.chillguys.nebulazone.application.bid.consts.BidConst.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.application.auction.service.AuctionRedisService;
import nbc.chillguys.nebulazone.application.bid.dto.response.CreateBidResponse;
import nbc.chillguys.nebulazone.application.bid.dto.response.DeleteBidResponse;
import nbc.chillguys.nebulazone.application.bid.dto.response.FindBidResponse;
import nbc.chillguys.nebulazone.domain.bid.entity.BidStatus;
import nbc.chillguys.nebulazone.domain.bid.exception.BidErrorCode;
import nbc.chillguys.nebulazone.domain.bid.exception.BidException;
import nbc.chillguys.nebulazone.domain.user.entity.User;
import nbc.chillguys.nebulazone.domain.user.service.UserDomainService;
import nbc.chillguys.nebulazone.infra.redis.vo.AuctionVo;
import nbc.chillguys.nebulazone.infra.redis.vo.BidVo;

@Service
@RequiredArgsConstructor
public class BidRedisService {

	private final RedisTemplate<String, Object> redisTemplate;
	private final ObjectMapper objectMapper;
	private final RedissonClient redissonClient;
	private final UserDomainService userDomainService;

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
	@Transactional
	public CreateBidResponse createBid(Long auctionId, User user, Long bidPrice) {

		RLock bidLock = redissonClient.getLock(BID_LOCK_PREFIX + auctionId + ":" + user.getId());
		User loginUser = userDomainService.findActiveUserById(user.getId());

		acquireLockOrThrow(bidLock);

		try {
			AuctionVo auctionVo = auctionRedisService.getAuctionVoElseThrow(auctionId);

			auctionVo.validAuctionNotClosed();
			auctionVo.validWonAuction();
			auctionVo.validAuctionOwnerNotBid(user.getId());
			auctionVo.validMinimumBidPrice(bidPrice);

			String bidKey = BID_PREFIX + auctionId;

			Set<Object> allBid = redisTemplate.opsForZSet().range(bidKey, 0, -1);
			BidVo findBidVo = Optional.ofNullable(allBid)
				.orElse(Set.of())
				.stream()
				.map(o -> objectMapper.convertValue(o, BidVo.class))
				.filter(bidVo -> bidVo.getBidUserId().equals(user.getId()))
				.filter(bidVo -> bidVo.getBidStatus().equals(BidStatus.BID.name()))
				.findFirst()
				.orElse(null);

			if (findBidVo != null) {
				loginUser.addPoint(findBidVo.getBidPrice());
				redisTemplate.opsForZSet().remove(bidKey, findBidVo);
				findBidVo.cancelBid();
				redisTemplate.opsForZSet().add(bidKey, findBidVo, findBidVo.getBidPrice());
			}

			BidVo bidVo = BidVo.of(auctionId, user, bidPrice);

			redisTemplate.opsForZSet().add(bidKey, bidVo, bidPrice);

			auctionRedisService.updateAuctionCurrentPrice(auctionId, bidPrice);
			loginUser.usePoint(bidPrice);

			return CreateBidResponse.from(bidVo);

		} finally {
			bidLock.unlock();
		}

	}

	/**
	 * redis 경매 입찰 취소<br>
	 * Redisson RLock을 사용하여 동시성 제어
	 * @param user 로그인 유저(입찰 취소 요청자)
	 * @param auctionId 입찰 취소 대상 경매 id
	 * @param bidPrice 취소할 입찰 가격
	 * @return DeleteBidResponse
	 * @author 전나겸
	 */
	@Transactional
	public DeleteBidResponse statusBid(User user, Long auctionId, Long bidPrice) {
		RLock bidLock = redissonClient.getLock(BID_DELETE_LOCK_PREFIX + auctionId);
		User loginUser = userDomainService.findActiveUserById(user.getId());

		acquireLockOrThrow(bidLock);

		try {
			AuctionVo auctionVo = auctionRedisService.getAuctionVoElseThrow(auctionId);

			auctionVo.validAuctionNotClosed();
			auctionVo.validWonAuction();
			auctionVo.validBidCancelBefore30Minutes();

			String bidKey = BID_PREFIX + auctionId;
			Set<Object> objectBidVo = redisTemplate.opsForZSet().rangeByScore(bidKey, bidPrice, bidPrice);

			BidVo findBidVo = Optional.ofNullable(objectBidVo)
				.orElse(Set.of())
				.stream()
				.map(o -> objectMapper.convertValue(o, BidVo.class))
				.filter(bidVo -> BidStatus.BID.name().equals(bidVo.getBidStatus()))
				.filter(bidVo -> bidVo.getBidUserId().equals(user.getId()))
				.findFirst()
				.orElseThrow(() -> new BidException(BidErrorCode.BID_NOT_FOUND));

			findBidVo.validNotBidOwner(user.getId());
			findBidVo.validBidStatusIsCancel();
			findBidVo.validBidStatusIsWon();
			findBidVo.validAuctionMismatch(auctionId);

			redisTemplate.opsForZSet().remove(bidKey, findBidVo);
			findBidVo.cancelBid();
			redisTemplate.opsForZSet().add(bidKey, findBidVo, findBidVo.getBidPrice());

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
			loginUser.addPoint(bidPrice);

			return DeleteBidResponse.from(findBidVo);

		} finally {
			bidLock.unlock();
		}
	}

	/**
	 * redis의 특정 경매의 입찰 내역 조회 후 페이징 반환
	 * @param auctionId 조회할 대상 경매 id
	 * @param page 페이지
	 * @param size 출력 개수
	 * @return 페이징 응답값
	 * @author 전나겸
	 */
	public Page<FindBidResponse> findBidsByAuctionId(Long auctionId, int page, int size) {

		String bidKey = BID_PREFIX + auctionId;
		Long totalElements = redisTemplate.opsForZSet().zCard(bidKey);

		if (totalElements == null) {
			return new PageImpl<>(List.of());
		}

		Set<Object> objects = redisTemplate.opsForZSet().reverseRange(bidKey, 0, -1);
		List<BidVo> bidVoList = Optional.ofNullable(objects)
			.orElse(Set.of())
			.stream()
			.map(o -> objectMapper.convertValue(o, BidVo.class))
			.sorted(Comparator
				.comparing(BidVo::getBidCreatedAt).reversed()
				.thenComparing(BidVo::getBidPrice).reversed())
			.skip((long)page * size)
			.limit(size)
			.toList();

		Pageable pageable = PageRequest.of(page, size);

		List<FindBidResponse> findBidResponse = bidVoList.stream()
			.map(FindBidResponse::from)
			.toList();

		return new PageImpl<>(findBidResponse, pageable, totalElements);
	}

	public List<BidVo> findMyBidVoList(Long userId, List<Long> auctionIds) {

		List<BidVo> myBidVoList = new ArrayList<>();

		for (Long auctionId : auctionIds) {
			String bidKey = BID_PREFIX + auctionId;
			Set<Object> objects = redisTemplate.opsForZSet().range(bidKey, 0, -1);

			List<BidVo> myBidsByAuctionId = Optional.ofNullable(objects)
				.orElse(Set.of())
				.stream()
				.map(o -> objectMapper.convertValue(o, BidVo.class))
				.filter(bidVo -> bidVo.getBidUserId().equals(userId))
				.toList();

			myBidVoList.addAll(myBidsByAuctionId);
		}

		return myBidVoList.stream()
			.sorted(Comparator.comparing(BidVo::getBidCreatedAt).reversed())
			.toList();

	}

	/**
	 * redis에 저장된 특정 경매의 낙찰 예정인 입찰 정보 조회
	 * @param auctionId 경매 id
	 * @param bidPrice 낙찰 예정인 입찰 가격
	 * @return 조회된 BidVo
	 * @author 전나겸
	 */
	public BidVo findWonBidVo(Long auctionId, Long bidPrice) {
		Set<Object> objects = redisTemplate.opsForZSet().rangeByScore(BID_PREFIX + auctionId, bidPrice, bidPrice);

		return Optional.ofNullable(objects)
			.orElse(Set.of())
			.stream()
			.map(o -> objectMapper.convertValue(o, BidVo.class))
			.findFirst()
			.orElse(null);

	}

	private void acquireLockOrThrow(RLock bidLock) {
		if (!bidLock.tryLock()) {
			throw new BidException(BidErrorCode.BID_PROCESSING_BUSY);
		}
	}

}
