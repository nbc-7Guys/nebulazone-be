package nbc.chillguys.nebulazone.application.auction.service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nbc.chillguys.nebulazone.application.auction.dto.request.ManualEndAuctionRequest;
import nbc.chillguys.nebulazone.application.auction.dto.response.DeleteAuctionResponse;
import nbc.chillguys.nebulazone.application.auction.dto.response.EndAuctionResponse;
import nbc.chillguys.nebulazone.application.auction.dto.response.FindSortTypeAuctionResponse;
import nbc.chillguys.nebulazone.domain.auction.dto.AuctionAdminUpdateCommand;
import nbc.chillguys.nebulazone.domain.auction.entity.Auction;
import nbc.chillguys.nebulazone.domain.auction.entity.AuctionSortType;
import nbc.chillguys.nebulazone.domain.auction.exception.AuctionErrorCode;
import nbc.chillguys.nebulazone.domain.auction.exception.AuctionException;
import nbc.chillguys.nebulazone.domain.auction.service.AuctionAdminDomainService;
import nbc.chillguys.nebulazone.domain.auction.service.AuctionDomainService;
import nbc.chillguys.nebulazone.domain.bid.entity.BidStatus;
import nbc.chillguys.nebulazone.domain.bid.exception.BidErrorCode;
import nbc.chillguys.nebulazone.domain.bid.exception.BidException;
import nbc.chillguys.nebulazone.domain.bid.service.BidDomainService;
import nbc.chillguys.nebulazone.domain.product.entity.Product;
import nbc.chillguys.nebulazone.domain.product.entity.ProductEndTime;
import nbc.chillguys.nebulazone.domain.product.event.ProductDeletedEvent;
import nbc.chillguys.nebulazone.domain.product.event.ProductUpdatedEvent;
import nbc.chillguys.nebulazone.domain.product.service.ProductDomainService;
import nbc.chillguys.nebulazone.domain.transaction.dto.TransactionCreateCommand;
import nbc.chillguys.nebulazone.domain.transaction.entity.UserType;
import nbc.chillguys.nebulazone.domain.transaction.service.TransactionDomainService;
import nbc.chillguys.nebulazone.domain.user.entity.User;
import nbc.chillguys.nebulazone.domain.user.service.UserDomainService;
import nbc.chillguys.nebulazone.infra.redis.constant.AuctionConstants;
import nbc.chillguys.nebulazone.infra.redis.constant.BidConstants;
import nbc.chillguys.nebulazone.infra.redis.dto.CreateRedisAuctionDto;
import nbc.chillguys.nebulazone.infra.redis.dto.FindAllAuctionsDto;
import nbc.chillguys.nebulazone.infra.redis.lock.DistributedLock;
import nbc.chillguys.nebulazone.infra.redis.publisher.RedisMessagePublisher;
import nbc.chillguys.nebulazone.infra.redis.service.UserCacheService;
import nbc.chillguys.nebulazone.infra.redis.vo.AuctionVo;
import nbc.chillguys.nebulazone.infra.redis.vo.BidVo;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuctionRedisService {

	private final RedisTemplate<String, Object> redisTemplate;
	private final ObjectMapper objectMapper;

	private final AuctionDomainService auctionDomainService;
	private final AuctionAdminDomainService auctionAdminDomainService;
	private final UserDomainService userDomainService;
	private final BidDomainService bidDomainService;
	private final TransactionDomainService transactionDomainService;
	private final ProductDomainService productDomainService;
	private final UserCacheService userCacheService;
	private final RedisMessagePublisher redisMessagePublisher;

	private final ApplicationEventPublisher eventPublisher;

	/**
	 * redis 경매 생성<br>
	 * hash로 auctionVo 저장, ZSet으로 경매 종료 순서를 관리
	 *
	 * @param redisAuctionDto 경매 생성을 위한 요청값
	 * @author 전나겸
	 */
	public void createAuction(CreateRedisAuctionDto redisAuctionDto) {

		Product product = redisAuctionDto.product();
		Auction auction = redisAuctionDto.auction();
		User user = redisAuctionDto.user();
		ProductEndTime productEndTime = redisAuctionDto.productEndTime();

		String auctionKey = AuctionConstants.AUCTION_PREFIX + auction.getId();
		AuctionVo auctionVo = AuctionVo.of(product, auction, user, List.of());

		Map<String, Object> auctionVoMap = objectMapper.convertValue(auctionVo, new TypeReference<>() {
		});
		redisTemplate.opsForHash().putAll(auctionKey, auctionVoMap);

		long endTimestamp = System.currentTimeMillis() / 1000 + productEndTime.getSeconds();
		redisTemplate.opsForZSet().add(AuctionConstants.AUCTION_ENDING_PREFIX, auction.getId(), endTimestamp);

	}

	/**
	 * 수동 낙찰
	 *
	 * @param auctionId 종료할 경매 id
	 * @param loginUser 로그인 유저
	 * @param request 낙찰 요청 reqeust
	 * @return 수동 낙찰 응답값
	 * @author 전나겸
	 */
	@Transactional
	@DistributedLock(key = "'auction:ending:lock:' + #auctionId")
	public EndAuctionResponse manualEndAuction(Long auctionId, User loginUser, ManualEndAuctionRequest request) {

		Map<Object, Object> auctionMap = redisTemplate.opsForHash()
			.entries(AuctionConstants.AUCTION_PREFIX + auctionId);
		AuctionVo auctionVo = objectMapper.convertValue(auctionMap, AuctionVo.class);

		auctionVo.validNotAuctionOwner(loginUser);
		auctionVo.validMismatchBidPrice(request.bidPrice());
		auctionVo.validAuctionNotClosed();
		auctionVo.validWonAuction();

		Auction auction = auctionDomainService.manualEndAuction(auctionId, request.bidPrice());
		Product wonAuctionProduct = auction.getProduct();
		wonAuctionProduct.purchase();

		try {
			productDomainService.markProductAsPurchased(wonAuctionProduct.getId());
		} catch (Exception e) {
			log.info("수동 낙찰 완료, ES에 판매 완료로 변경 중 에러발생, productId: {}", wonAuctionProduct.getId(), e);
		}

		User seller = wonAuctionProduct.getSeller();
		seller.plusPoint(auction.getCurrentPrice());
		userCacheService.deleteUserById(seller.getId());

		Set<Object> objects = redisTemplate.opsForZSet().range(BidConstants.BID_PREFIX + auctionId, 0, -1);

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
			.filter(bidVo -> BidStatus.WON.name().equals(bidVo.getBidStatus()))
			.findFirst()
			.orElseThrow(() -> new BidException(BidErrorCode.BID_NOT_FOUND));

		wonBidVo.validMismatchBidUsername(request.bidUserNickname());

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
					userCacheService.deleteUserById(bidUser.getId());
				}
			});

		bidDomainService.createAllBid(auction, bidVoList, userMap);

		TransactionCreateCommand sellerTxCreateCommand = TransactionCreateCommand.of(seller, UserType.SELLER,
			wonAuctionProduct, wonAuctionProduct.getTxMethod().name(),
			auction.getCurrentPrice(), LocalDateTime.now());

		transactionDomainService.createTransaction(sellerTxCreateCommand);

		redisTemplate.opsForZSet().remove(AuctionConstants.AUCTION_ENDING_PREFIX, auctionId);
		redisTemplate.delete(AuctionConstants.AUCTION_PREFIX + auctionId);
		redisTemplate.delete(BidConstants.BID_PREFIX + auctionId);

		EndAuctionResponse response = EndAuctionResponse.of(auction, wonBidVo, wonAuctionProduct);

		try {
			redisMessagePublisher.publishAuctionUpdate(auctionId, "won", response);
			log.info("수동 낙찰 WebSocket 메시지 발행 성공 - auctionId: {}", auctionId);
		} catch (Exception e) {
			log.error("수동 낙찰 WebSocket 브로드캐스트 실패 - auctionId: {}", auctionId, e);
		}

		try {
			redisMessagePublisher.publishAuctionUpdate(auctionId, "bid", response);
			log.info("수동 낙찰 입찰 상태 업데이트 WebSocket 메시지 발행 성공 - auctionId: {}", auctionId);
		} catch (Exception e) {
			log.error("수동 낙찰 입찰 상태 업데이트 WebSocket 브로드캐스트 실패 - auctionId: {}", auctionId, e);
		}

		return response;

	}

	/**
	 * 내 경매 삭제
	 *
	 * @param auctionId 삭제할 경매 id
	 * @param loginUser 로그인 유저
	 * @return 경매 삭제 응답 값
	 * @author 전나겸
	 */
	@Transactional
	@DistributedLock(key = "'auction:delete:lock:' + #auctionId")
	public DeleteAuctionResponse deleteAuction(Long auctionId, User loginUser) {

		Map<Object, Object> entries = redisTemplate.opsForHash()
			.entries(AuctionConstants.AUCTION_PREFIX + auctionId);
		AuctionVo auctionVo = objectMapper.convertValue(entries, AuctionVo.class);

		auctionVo.validNotAuctionOwner(loginUser);
		Auction deletedAuction = auctionDomainService.deleteAuction(auctionId);

		if (auctionVo.getCurrentPrice() >= auctionVo.getStartPrice()) {
			Set<Object> objects = redisTemplate.opsForZSet().range(BidConstants.BID_PREFIX + auctionId, 0, -1);

			List<BidVo> bidVoList = Optional.ofNullable(objects)
				.orElse(Set.of())
				.stream()
				.map(bid -> objectMapper.convertValue(bid, BidVo.class))
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
					if (BidStatus.BID.name().equals(bidVo.getBidStatus())) {
						User bidUser = userMap.get(bidVo.getBidUserId());
						bidUser.plusPoint(bidVo.getBidPrice());
						userCacheService.deleteUserById(bidUser.getId());
					}
				});

			bidDomainService.createAllBid(deletedAuction, bidVoList, userMap);
		}

		Product product = deletedAuction.getProduct();
		product.delete();
		eventPublisher.publishEvent(new ProductDeletedEvent(product.getId()));

		redisTemplate.opsForZSet().remove(AuctionConstants.AUCTION_ENDING_PREFIX, auctionId);
		redisTemplate.delete(AuctionConstants.AUCTION_PREFIX + auctionId);
		redisTemplate.delete(BidConstants.BID_PREFIX + auctionId);

		DeleteAuctionResponse response = DeleteAuctionResponse.of(deletedAuction.getId(), product.getId());

		try {
			redisMessagePublisher.publishAuctionUpdate(auctionId, "deleted", response);
			log.info("경매 삭제 WebSocket 메시지 발행 성공 - auctionId: {}", auctionId);
		} catch (Exception e) {
			log.error("경매 삭제 WebSocket 메시지 발행 실패 - auctionId: {}, error: {}", auctionId, e.getMessage(), e);
		}

		return response;

	}

	/**
	 * 정렬기반 경매 조회<br>
	 * CLOSING: 마감임박순, POPULAR: 인기순(입찰 건수)
	 *
	 * @param sortType 정렬 타입
	 * @return 조회된 응답값
	 * @author 전나겸
	 */
	public FindSortTypeAuctionResponse findAuctionsBySortType(AuctionSortType sortType) {

		String findAuctionsBySortTypeKey = AuctionConstants.AUCTION_FIND_SORT_TYPE_PREFIX + sortType.name();

		Object object = redisTemplate.opsForValue().get(findAuctionsBySortTypeKey);

		if (object != null) {
			FindSortTypeAuctionResponse cacheData = objectMapper
				.convertValue(object, FindSortTypeAuctionResponse.class);

			if (!cacheData.auctions().isEmpty()) {
				return cacheData;
			}
		}

		FindSortTypeAuctionResponse response = switch (sortType) {
			case CLOSING -> {
				Set<Object> objects = redisTemplate.opsForZSet().range(AuctionConstants.AUCTION_ENDING_PREFIX, 0, 4);

				List<FindAllAuctionsDto> findAuctionsByClosing = Optional.ofNullable(objects)
					.orElse(Set.of())
					.stream()
					.map(obj -> {
						long auctionId = ((Number)obj).longValue();
						AuctionVo auctionVo = getAuctionVoElseThrow(auctionId);
						Long bidCount = calculateAuctionBidCount(auctionId);
						return FindAllAuctionsDto.of(auctionVo, bidCount);
					})
					.toList();

				yield FindSortTypeAuctionResponse.from(findAuctionsByClosing);
			}

			case POPULAR -> {
				Set<Object> objects = redisTemplate.opsForZSet().range(AuctionConstants.AUCTION_ENDING_PREFIX, 0, -1);

				List<FindAllAuctionsDto> findAuctionsByPopular = Optional.ofNullable(objects)
					.orElse(Set.of())
					.stream()
					.map(obj -> {
						long auctionId = ((Number)obj).longValue();
						AuctionVo auctionVo = getAuctionVoElseThrow(auctionId);
						Long bidCount = calculateAuctionBidCount(auctionId);
						return FindAllAuctionsDto.of(auctionVo, bidCount);
					})
					.sorted(Comparator.comparing(FindAllAuctionsDto::bidCount).reversed())
					.limit(5)
					.toList();

				yield FindSortTypeAuctionResponse.from(findAuctionsByPopular);
			}
		};

		redisTemplate.opsForValue()
			.set(findAuctionsBySortTypeKey, response, AuctionConstants.AUCTION_FIND_SORT_TYPE_TTL);
		return response;
	}

	/**
	 * redis에 저장된 특정 경매를 조회<br>
	 * AuctionVo를 못찾으면 에러를 반환
	 *
	 * @param auctionId 조회할 경매 id
	 * @return AuctionVo
	 * @author 전나겸
	 */
	public AuctionVo getAuctionVoElseThrow(Long auctionId) {
		Map<Object, Object> auctionMap = redisTemplate.opsForHash()
			.entries(AuctionConstants.AUCTION_PREFIX + auctionId);

		if (auctionMap.isEmpty()) {
			throw new AuctionException(AuctionErrorCode.AUCTION_NOT_FOUND);
		}

		return objectMapper.convertValue(auctionMap, AuctionVo.class);
	}

	/**
	 * redis에 저장된 특정 경매를 조회
	 *
	 * @param auctionId 조회할 경매 id
	 * @return 조회된 AuctionVo
	 * @author 전나겸
	 */
	public AuctionVo findRedisAuctionVo(Long auctionId) {
		Map<Object, Object> auctionMap = redisTemplate.opsForHash()
			.entries(AuctionConstants.AUCTION_PREFIX + auctionId);

		if (auctionMap.isEmpty()) {
			return null;
		}

		return objectMapper.convertValue(auctionMap, AuctionVo.class);
	}

	/**
	 * 특정 경매의 입찰 최고가 갱신
	 *
	 * @param auctionId 대상 경매 id
	 * @param bidPrice 갱신할 입찰가 (null 가능 - 입찰이 모두 취소된 경우)
	 * @author 전나겸
	 */
	public void updateAuctionCurrentPrice(Long auctionId, Long bidPrice) {
		String auctionKey = AuctionConstants.AUCTION_PREFIX + auctionId;

		redisTemplate.opsForHash().put(auctionKey, "currentPrice", bidPrice);

	}

	/**
	 * 레디스의 전체 경매 id들 조회
	 *
	 * @return 조회된 경매 id 리스트
	 * @author 전나겸
	 */
	public List<Long> findAllAuctionVoIds() {
		Set<Object> objects = redisTemplate.opsForZSet().range(AuctionConstants.AUCTION_ENDING_PREFIX, 0, -1);

		return Optional.ofNullable(objects)
			.orElse(Set.of())
			.stream()
			.map(obj -> ((Number)obj).longValue())
			.toList();

	}

	/**
	 * 특정 경매의 입찰 건수를 계산
	 * @param auctionId 대상 경매 id
	 * @return 해당 경매의 입찰 건수
	 * @author 전나겸
	 */
	public Long calculateAuctionBidCount(Long auctionId) {
		Set<Object> objects = redisTemplate.opsForZSet()
			.range(BidConstants.BID_PREFIX + auctionId, 0, -1);

		return (long)Optional.ofNullable(objects)
			.orElse(Set.of())
			.size();
	}

	/**
	 * 특졍 경매의 이미지 url 수정
	 *
	 * @param auctionId 대상 경매 id
	 * @param productImages 경매 상품 url 리스트
	 * @author 전나겸
	 */
	public void updateAuctionProductImages(Long auctionId, List<String> productImages) {
		String auctionKey = AuctionConstants.AUCTION_PREFIX + auctionId;

		redisTemplate.opsForHash().put(auctionKey, "productImageUrls", productImages);
	}

	/**
	 * 어드민 경매 삭제 처리
	 *
	 * <p>
	 * 1. Redisson 분산락을 이용해 동시 삭제 방지<br>
	 * 2. 경매 도메인에서 Auction 삭제 처리 및 관련 엔티티 상태 반영<br>
	 * 3. 레디스에서 해당 경매의 입찰 이력(BID ZSet) 조회 후,
	 *    - 입찰자 포인트 반환 (진행 중인 입찰에 한함)
	 *    - 입찰 이력 생성 및 저장
	 * 4. 연관 상품(Product)도 함께 논리 삭제 및 Elasticsearch에서 삭제<br>
	 * 5. 레디스에서 경매/입찰 데이터 정리<br>
	 * </p>
	 *
	 * @param auctionId 삭제할 경매 ID
	 * @author 정석현
	 */
	@Transactional
	@DistributedLock(key = "'auction:delete:admin:lock:' + #auctionId")
	public void deleteAdminAuction(Long auctionId) {

		Auction deletedAuction = auctionDomainService.deleteAuction(auctionId);

		Set<Object> bidObjects = redisTemplate.opsForZSet().range(BidConstants.BID_PREFIX + auctionId, 0, -1);

		if (bidObjects != null && !bidObjects.isEmpty()) {
			List<BidVo> bidVoList = bidObjects.stream()
				.map(bid -> objectMapper.convertValue(bid, BidVo.class))
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
					if (BidStatus.BID.name().equals(bidVo.getBidStatus())) {
						User bidUser = userMap.get(bidVo.getBidUserId());
						bidUser.plusPoint(bidVo.getBidPrice());
						userCacheService.deleteUserById(bidUser.getId());
					}
				});

			bidDomainService.createAllBid(deletedAuction, bidVoList, userMap);
		}

		Product product = deletedAuction.getProduct();
		product.delete();
		eventPublisher.publishEvent(new ProductDeletedEvent(product.getId()));

		redisTemplate.opsForZSet().remove(AuctionConstants.AUCTION_ENDING_PREFIX, auctionId);
		redisTemplate.delete(AuctionConstants.AUCTION_PREFIX + auctionId);
		redisTemplate.delete(BidConstants.BID_PREFIX + auctionId);

		try {
			DeleteAuctionResponse response = DeleteAuctionResponse.of(deletedAuction.getId(), product.getId());
			redisMessagePublisher.publishAuctionUpdate(auctionId, "deleted", response);
			log.info("관리자 경매 삭제 WebSocket 메시지 발행 성공 - auctionId: {}", auctionId);
		} catch (Exception e) {
			log.error("관리자 경매 삭제 WebSocket 메시지 발행 실패 - auctionId: {}, error: {}", auctionId, e.getMessage(), e);
		}

	}

	/**
	 * 어드민 경매 정보 수정
	 *
	 * <p>
	 * 1. 경매 Vo 객체의 검증 메서드(`validateUpdatableByAdmin`)로 비즈니스 제약 체크<br>
	 * 2. 수정값(시작가, 현재가, 종료시간 등) 적용<br>
	 * 3. 변경된 AuctionVo를 레디스에 반영(Hash, ZSet 갱신)<br>
	 * 4. DB/Elasticsearch 등 영속 데이터 동기화<br>
	 * </p>
	 *
	 * @param auctionId 수정할 경매 ID
	 * @param command   어드민이 입력한 수정 정보(시작가, 현재가, 종료시간 등)
	 * @author 정석현
	 */
	public void updateAdminAuction(Long auctionId, AuctionAdminUpdateCommand command) {
		String auctionKey = AuctionConstants.AUCTION_PREFIX + auctionId;
		AuctionVo auctionVo = getAuctionVoElseThrow(auctionId);

		auctionVo.validateUpdatableByAdmin(command);

		auctionVo.updateByAdmin(command.startPrice(), command.currentPrice(), command.endTime());

		Map<String, Object> auctionVoMap = objectMapper.convertValue(auctionVo, new TypeReference<>() {
		});
		redisTemplate.opsForHash().putAll(auctionKey, auctionVoMap);

		if (command.endTime() != null) {
			long score = command.endTime().atZone(ZoneId.of("Asia/Seoul")).toEpochSecond();
			redisTemplate.opsForZSet().add(AuctionConstants.AUCTION_ENDING_PREFIX, auctionId, score);
		}

		Product product = auctionAdminDomainService.updateAuction(auctionId, command);
		eventPublisher.publishEvent(new ProductUpdatedEvent(product));
	}

}
