package nbc.chillguys.nebulazone.application.bid.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nbc.chillguys.nebulazone.application.auction.service.AuctionRedisService;
import nbc.chillguys.nebulazone.application.bid.dto.response.CreateBidResponse;
import nbc.chillguys.nebulazone.application.bid.dto.response.DeleteBidResponse;
import nbc.chillguys.nebulazone.application.bid.dto.response.FindBidResponse;
import nbc.chillguys.nebulazone.application.bid.metrics.BidMetrics;
import nbc.chillguys.nebulazone.application.bid.metrics.TrackBidMetrics;
import nbc.chillguys.nebulazone.domain.bid.entity.BidStatus;
import nbc.chillguys.nebulazone.domain.bid.exception.BidErrorCode;
import nbc.chillguys.nebulazone.domain.bid.exception.BidException;
import nbc.chillguys.nebulazone.domain.user.entity.User;
import nbc.chillguys.nebulazone.domain.user.service.UserDomainService;
import nbc.chillguys.nebulazone.infra.redis.constant.BidConstants;
import nbc.chillguys.nebulazone.infra.redis.lock.DistributedLock;
import nbc.chillguys.nebulazone.infra.redis.publisher.RedisMessagePublisher;
import nbc.chillguys.nebulazone.infra.redis.service.UserCacheService;
import nbc.chillguys.nebulazone.infra.redis.vo.AuctionVo;
import nbc.chillguys.nebulazone.infra.redis.vo.BidVo;

@Slf4j
@Service
@RequiredArgsConstructor
public class BidRedisService {

	private final RedisTemplate<String, Object> redisTemplate;
	private final ObjectMapper objectMapper;
	private final UserDomainService userDomainService;
	private final UserCacheService userCacheService;
	private final AuctionRedisService auctionRedisService;
	private final RedisMessagePublisher redisMessagePublisher;
	private final BidMetrics bidMetrics;

	/**
	 * redis 경매 입찰
	 *
	 * @param auctionId 입찰 대상 경매 id
	 * @param user 로그인 유저(입찰 유저)
	 * @param bidPrice 입찰가
	 * @return CreateBidResponse
	 * @author 전나겸
	 */
	@Transactional
	@TrackBidMetrics
	@DistributedLock(key = "'bid:lock:auction:' + #auctionId")
	public CreateBidResponse createBid(Long auctionId, User user, Long bidPrice) {
		User loginUser = userDomainService.findActiveUserById(user.getId());

		AuctionVo auctionVo = auctionRedisService.getAuctionVoElseThrow(auctionId);

		auctionVo.validAuctionNotClosed();
		auctionVo.validWonAuction();
		auctionVo.validAuctionOwnerNotBid(user.getId());
		auctionVo.validMinimumBidPrice(bidPrice);

		String bidKey = BidConstants.BID_PREFIX + auctionId;

		Set<Object> allBid = redisTemplate.opsForZSet().range(bidKey, 0, -1);

		BidVo bidVo = Optional.ofNullable(allBid)
			.orElse(Set.of())
			.stream()
			.map(o -> objectMapper.convertValue(o, BidVo.class))
			.filter(bv -> bv.getBidUserId().equals(user.getId()))
			.filter(bv -> bv.getBidStatus().equals(BidStatus.BID.name()))
			.findFirst()
			.map(bv -> {
				redisTemplate.opsForZSet().remove(bidKey, bv);
				bv.cancelBid();
				redisTemplate.opsForZSet().add(bidKey, bv, bv.getBidPrice());
				loginUser.minusPoint(bidPrice - bv.getBidPrice());

				BidVo newBidVo = BidVo.of(auctionId, user, bidPrice);
				redisTemplate.opsForZSet().add(bidKey, newBidVo, bidPrice);

				return newBidVo;
			})
			.orElseGet(() -> {
				BidVo newBidVo = BidVo.of(auctionId, user, bidPrice);
				redisTemplate.opsForZSet().add(bidKey, newBidVo, bidPrice);

				loginUser.minusPoint(bidPrice);

				return newBidVo;
			});

		userCacheService.deleteUserById(loginUser.getId());

		auctionRedisService.updateAuctionCurrentPrice(auctionId, bidPrice);

		CreateBidResponse response = CreateBidResponse.from(bidVo);

		try {
			redisMessagePublisher.publishAuctionUpdate(auctionId, "bid", response);
		} catch (Exception e) {
			log.error("입찰 WebSocket 브로드캐스트 실패 - auctionId: {}", auctionId, e);
		}

		return response;
	}

	/**
	 * redis 경매 입찰 취소
	 *
	 * @param user 로그인 유저(입찰 취소 요청자)
	 * @param auctionId 입찰 취소 대상 경매 id
	 * @param bidPrice 취소할 입찰 가격
	 * @return DeleteBidResponse
	 * @author 전나겸
	 */
	@Transactional
	@DistributedLock(key = "'bid:deleteLock:' + #auctionId")
	public DeleteBidResponse statusBid(User user, Long auctionId, Long bidPrice) {
		User loginUser = userDomainService.findActiveUserById(user.getId());

		AuctionVo auctionVo = auctionRedisService.getAuctionVoElseThrow(auctionId);

		auctionVo.validAuctionNotClosed();
		auctionVo.validWonAuction();
		auctionVo.validBidCancelBefore30Minutes();

		String bidKey = BidConstants.BID_PREFIX + auctionId;
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
		loginUser.plusPoint(bidPrice);
		userCacheService.deleteUserById(loginUser.getId());

		DeleteBidResponse response = DeleteBidResponse.from(findBidVo);

		try {
			redisMessagePublisher.publishAuctionUpdate(auctionId, "bid", response);
		} catch (Exception e) {
			log.error("입찰 취소 WebSocket 브로드캐스트 실패 - auctionId: {}", auctionId, e);
		}

		bidMetrics.countBidCancel();
		return response;

	}

	/**
	 * redis의 특정 경매의 입찰 내역 조회 후 페이징 반환
	 *
	 * @param auctionId 조회할 대상 경매 id
	 * @param page 페이지
	 * @param size 출력 개수
	 * @return 페이징 응답값
	 * @author 전나겸
	 */
	public Page<FindBidResponse> findBidsByAuctionId(Long auctionId, int page, int size) {

		String bidKey = BidConstants.BID_PREFIX + auctionId;
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

	/**
	 * 내 입찰 내역 조회
	 *
	 * @param userId 로그인한 유저 id
	 * @param auctionIds 내가 입찰한 경매 id 리스트
	 * @return 반환 입찰 내역 리스트
	 * @author 전나겸
	 */
	public List<BidVo> findMyBidVoList(Long userId, List<Long> auctionIds) {

		List<BidVo> myBidVoList = new ArrayList<>();

		for (Long auctionId : auctionIds) {
			String bidKey = BidConstants.BID_PREFIX + auctionId;
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
	 *
	 * @param auctionId 경매 id
	 * @param bidPrice 낙찰 예정인 입찰 가격
	 * @return 조회된 BidVo
	 * @author 전나겸
	 */
	public BidVo findWonBidVo(Long auctionId, Long bidPrice) {
		Set<Object> objects = redisTemplate.opsForZSet()
			.rangeByScore(BidConstants.BID_PREFIX + auctionId, bidPrice, bidPrice);

		return Optional.ofNullable(objects)
			.orElse(Set.of())
			.stream()
			.map(o -> objectMapper.convertValue(o, BidVo.class))
			.findFirst()
			.orElse(null);

	}

	/**
	 * 어드민이 Redis에 저장된 특정 입찰(Bid)의 상태를 업데이트하는 메서드입니다.<br>
	 * 입찰은 (userId, price, bidCreatedAt) 조합으로 식별하며,<br>
	 * Redisson 분산락을 활용해 동시성 문제를 방지합니다.<br>
	 *
	 * @param auctionId     경매 ID
	 * @param userId        입찰자(유저) ID
	 * @param price         입찰 가격
	 * @param bidCreatedAt  입찰 생성 시각
	 * @param status        변경할 입찰 상태 (예: "BID", "CANCEL", "WON" 등)
	 * @author 정석현
	 */
	@DistributedLock(key = "'bid:lock:auction:' + #auctionId")
	public void updateBidStatusByAdmin(Long auctionId, Long userId, Long price, LocalDateTime bidCreatedAt,
		String status) {

		String bidKey = BidConstants.BID_PREFIX + auctionId;
		Set<Object> allBids = redisTemplate.opsForZSet().range(bidKey, 0, -1);

		BidVo targetBid = Optional.ofNullable(allBids).orElse(Set.of()).stream()
			.map(o -> objectMapper.convertValue(o, BidVo.class))
			.filter(b -> b.getBidUserId().equals(userId))
			.filter(b -> b.getBidPrice().equals(price))
			.filter(b -> b.getBidCreatedAt().isEqual(bidCreatedAt))
			.findFirst()
			.orElseThrow(() -> new BidException(BidErrorCode.BID_NOT_FOUND));

		redisTemplate.opsForZSet().remove(bidKey, targetBid);
		targetBid.updateStatus(status);
		redisTemplate.opsForZSet().add(bidKey, targetBid, targetBid.getBidPrice());

	}

	/**
	 * 어드민이 Redis에 저장된 특정 입찰(Bid)을 삭제하는 메서드입니다.<br>
	 * 입찰은 (userId, price, bidCreatedAt) 조합으로 식별합니다.<br>
	 * 입찰 상태가 BID(진행중)였다면, 삭제 후 현재가를 재계산해 경매 정보도 최신화합니다.<br>
	 * Redisson 분산락을 활용해 동시성 문제를 방지합니다.<br>
	 *
	 * @param auctionId     경매 ID
	 * @param userId        입찰자(유저) ID
	 * @param price         입찰 가격
	 * @param bidCreatedAt  입찰 생성 시각
	 * @author 정석현
	 */
	@DistributedLock(key = "'bid:lock:auction:' + #auctionId")
	public void cancelStatusBidBidByAdmin(Long auctionId, Long userId, Long price, LocalDateTime bidCreatedAt) {

		String bidKey = BidConstants.BID_PREFIX + auctionId;
		Set<Object> allBids = redisTemplate.opsForZSet().range(bidKey, 0, -1);

		BidVo targetBid = Optional.ofNullable(allBids).orElse(Set.of()).stream()
			.map(o -> objectMapper.convertValue(o, BidVo.class))
			.filter(b -> b.getBidUserId().equals(userId))
			.filter(b -> b.getBidPrice().equals(price))
			.filter(b -> b.getBidCreatedAt().isEqual(bidCreatedAt))
			.findFirst()
			.orElseThrow(() -> new BidException(BidErrorCode.BID_NOT_FOUND));

		redisTemplate.opsForZSet().remove(bidKey, targetBid);

		if (targetBid.getBidStatus().equals(BidStatus.BID.name())) {
			Set<Object> remainingBids = redisTemplate.opsForZSet().reverseRange(bidKey, 0, -1);
			Optional<BidVo> newTopBid = Optional.ofNullable(remainingBids).orElse(Set.of()).stream()
				.map(o -> objectMapper.convertValue(o, BidVo.class))
				.filter(bidVo -> BidStatus.BID.name().equals(bidVo.getBidStatus()))
				.findFirst();

			Long newCurrentPrice = newTopBid.map(BidVo::getBidPrice).orElse(0L);
			auctionRedisService.updateAuctionCurrentPrice(auctionId, newCurrentPrice);
		}

	}
}
