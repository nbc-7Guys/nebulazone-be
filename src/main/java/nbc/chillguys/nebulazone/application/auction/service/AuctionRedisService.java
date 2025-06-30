package nbc.chillguys.nebulazone.application.auction.service;

import static nbc.chillguys.nebulazone.application.auction.consts.AuctionConst.*;
import static nbc.chillguys.nebulazone.application.bid.consts.BidConst.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
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
import nbc.chillguys.nebulazone.domain.product.service.ProductDomainService;
import nbc.chillguys.nebulazone.domain.transaction.dto.TransactionCreateCommand;
import nbc.chillguys.nebulazone.domain.transaction.entity.UserType;
import nbc.chillguys.nebulazone.domain.transaction.service.TransactionDomainService;
import nbc.chillguys.nebulazone.domain.user.entity.User;
import nbc.chillguys.nebulazone.domain.user.service.UserDomainService;
import nbc.chillguys.nebulazone.infra.redis.dto.CreateRedisAuctionDto;
import nbc.chillguys.nebulazone.infra.redis.dto.FindAllAuctionsDto;
import nbc.chillguys.nebulazone.infra.redis.publisher.RedisMessagePublisher;
import nbc.chillguys.nebulazone.infra.redis.vo.AuctionVo;
import nbc.chillguys.nebulazone.infra.redis.vo.BidVo;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuctionRedisService {

	private final RedisTemplate<String, Object> redisTemplate;
	private final ObjectMapper objectMapper;
	private final RedissonClient redissonClient;

	private final AuctionDomainService auctionDomainService;
	private final AuctionAdminDomainService auctionAdminDomainService;
	private final UserDomainService userDomainService;
	private final BidDomainService bidDomainService;
	private final TransactionDomainService transactionDomainService;
	private final ProductDomainService productDomainService;

	private final RedisMessagePublisher redisMessagePublisher;

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
		ProductEndTime productEndTime = redisAuctionDto.productEndTime();
		List<String> productImageUrls = redisAuctionDto.productImageUrls();

		String auctionKey = AUCTION_PREFIX + auction.getId();
		AuctionVo auctionVo = AuctionVo.of(product, auction, user, productImageUrls);

		Map<String, Object> auctionVoMap = objectMapper.convertValue(auctionVo, new TypeReference<>() {
		});
		redisTemplate.opsForHash().putAll(auctionKey, auctionVoMap);

		long endTimestamp = System.currentTimeMillis() / 1000 + productEndTime.getSeconds();
		redisTemplate.opsForZSet().add(AUCTION_ENDING_PREFIX, auction.getId(), endTimestamp);

	}

	/**
	 * 수동 낙찰<br>
	 * @param auctionId 종료할 경매 id
	 * @param loginUser 로그인 유저
	 * @param request 낙찰 요청 reqeust
	 * @return 수동 낙찰 응답값
	 * @author 전나겸
	 */
	@Transactional
	public EndAuctionResponse manualEndAuction(Long auctionId, User loginUser, ManualEndAuctionRequest request) {
		RLock auctionEndingLock = redissonClient.getLock(AUCTION_LOCK_ENDING_PREFIX + auctionId);

		try {
			acquireLockOrThrow(auctionEndingLock);

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
				.filter(bidVo -> BidStatus.WON.name().equals(bidVo.getBidStatus()))
				.findFirst()
				.orElseThrow(() -> new BidException(BidErrorCode.BID_NOT_FOUND));

			wonBidVo.validMismatchBidOwner(request.bidUserId());

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
						bidUser.addPoint(bidVo.getBidPrice());
					}
				});

			bidDomainService.createAllBid(auction, bidVoList, userMap);

			TransactionCreateCommand sellerTxCreateCommand = TransactionCreateCommand.of(seller, UserType.SELLER,
				wonAuctionProduct, wonAuctionProduct.getTxMethod().name(),
				auction.getCurrentPrice(), LocalDateTime.now());

			transactionDomainService.createTransaction(sellerTxCreateCommand);

			redisTemplate.opsForZSet().remove(AUCTION_ENDING_PREFIX, auctionId);
			redisTemplate.delete(AUCTION_PREFIX + auctionId);
			redisTemplate.delete(BID_PREFIX + auctionId);

			EndAuctionResponse response = EndAuctionResponse.of(auction, wonBidVo, wonAuctionProduct);

			try {
				redisMessagePublisher.publishAuctionUpdate(auctionId, "won", response);
			} catch (Exception e) {
				log.error("수동 낙찰 WebSocket 브로드캐스트 실패 - auctionId: {}", auctionId, e);
			}

			return response;

		} finally {
			auctionEndingLock.unlock();
		}
	}

	/**
	 * 내 경매 삭제
	 * @param auctionId 삭제할 경매 id
	 * @param loginUser 로그인 유저
	 * @return 경매 삭제 응답 값
	 * @author 전나겸
	 */
	@Transactional
	public DeleteAuctionResponse deleteAuction(Long auctionId, User loginUser) {
		RLock auctionDelete = redissonClient.getLock(AUCTION_LOCK_DELETE_PREFIX + auctionId);

		try {
			acquireLockOrThrow(auctionDelete);

			Map<Object, Object> entries = redisTemplate.opsForHash().entries(AUCTION_PREFIX + auctionId);
			AuctionVo auctionVo = objectMapper.convertValue(entries, AuctionVo.class);

			auctionVo.validNotAuctionOwner(loginUser);
			Auction deletedAuction = auctionDomainService.deleteAuction(auctionId);

			if (auctionVo.getCurrentPrice() >= auctionVo.getStartPrice()) {
				Set<Object> objects = redisTemplate.opsForZSet().range(BID_PREFIX + auctionId, 0, -1);

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
							bidUser.addPoint(bidVo.getBidPrice());
						}
					});

				bidDomainService.createAllBid(deletedAuction, bidVoList, userMap);
			}

			Product product = deletedAuction.getProduct();
			product.delete();

			productDomainService.deleteProductFromEs(product.getId());

			redisTemplate.opsForZSet().remove(AUCTION_ENDING_PREFIX, auctionId);
			redisTemplate.delete(AUCTION_PREFIX + auctionId);
			redisTemplate.delete(BID_PREFIX + auctionId);

			return DeleteAuctionResponse.of(deletedAuction.getId(), product.getId());
		} finally {
			auctionDelete.unlock();
		}

	}

	/**
	 * 정렬기반 경매 조회<br>
	 * CLOSING: 마감임박순, POPULAR: 인기순(입찰 건수)
	 * @param sortType 정렬 타입
	 * @return 조회된 응답값
	 * @author 전나겸
	 */
	public FindSortTypeAuctionResponse findAuctionsBySortType(AuctionSortType sortType) {

		String findAuctionsBySortTypeKey = AUCTION_FIND_SORT_TYPE_PREFIX + sortType.name();

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
				Set<Object> objects = redisTemplate.opsForZSet().range(AUCTION_ENDING_PREFIX, 0, 4);

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
				Set<Object> objects = redisTemplate.opsForZSet().range(AUCTION_ENDING_PREFIX, 0, -1);

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

		redisTemplate.opsForValue().set(findAuctionsBySortTypeKey, response, AUCTION_FIND_SORT_TYPE_TTL);
		return response;
	}

	/**
	 * redis에 저장된 특정 경매를 조회<br>
	 * AuctionVo를 못찾으면 에러를 반환
	 * @param auctionId 조회할 경매 id
	 * @return AuctionVo
	 * @author 전나겸
	 */
	public AuctionVo getAuctionVoElseThrow(Long auctionId) {
		Map<Object, Object> auctionMap = redisTemplate.opsForHash()
			.entries(AUCTION_PREFIX + auctionId);

		if (auctionMap.isEmpty()) {
			throw new AuctionException(AuctionErrorCode.AUCTION_NOT_FOUND);
		}

		return objectMapper.convertValue(auctionMap, AuctionVo.class);
	}

	/**
	 * redis에 저장된 특정 경매를 조회
	 * @param auctionId 조회할 경매 id
	 * @return 조회된 AuctionVo
	 * @author 전나겸
	 */
	public AuctionVo findRedisAuctionVo(Long auctionId) {
		Map<Object, Object> auctionMap = redisTemplate.opsForHash()
			.entries(AUCTION_PREFIX + auctionId);

		if (auctionMap.isEmpty()) {
			return null;
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
		String auctionKey = AUCTION_PREFIX + auctionId;

		redisTemplate.opsForHash().put(auctionKey, "currentPrice", bidPrice);

	}

	public List<Long> findAllAuctionVoIds() {
		Set<Object> objects = redisTemplate.opsForZSet().range(AUCTION_ENDING_PREFIX, 0, -1);

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
			.range(BID_PREFIX + auctionId, 0, -1);

		return (long)Optional.ofNullable(objects)
			.orElse(Set.of())
			.size();
	}

	@Transactional
	public void deleteAdminAuction(Long auctionId) {
		RLock auctionDeleteLock = redissonClient.getLock(AUCTION_LOCK_DELETE_PREFIX + auctionId);

		try {
			acquireLockOrThrow(auctionDeleteLock);

			Auction deletedAuction = auctionDomainService.deleteAuction(auctionId);

			Set<Object> bidObjects = redisTemplate.opsForZSet().range(BID_PREFIX + auctionId, 0, -1);

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
							bidUser.addPoint(bidVo.getBidPrice());
						}
					});

				bidDomainService.createAllBid(deletedAuction, bidVoList, userMap);
			}

			Product product = deletedAuction.getProduct();
			product.delete();
			productDomainService.deleteProductFromEs(product.getId());

			redisTemplate.opsForZSet().remove(AUCTION_ENDING_PREFIX, auctionId);
			redisTemplate.delete(AUCTION_PREFIX + auctionId);
			redisTemplate.delete(BID_PREFIX + auctionId);

		} finally {
			if (auctionDeleteLock.isLocked() && auctionDeleteLock.isHeldByCurrentThread()) {
				auctionDeleteLock.unlock();
			}
		}
	}

	public void updateAdminAuction(Long auctionId, AuctionAdminUpdateCommand command) {
		String auctionKey = AUCTION_PREFIX + auctionId;
		AuctionVo auctionVo = getAuctionVoElseThrow(auctionId);

		auctionVo.validateUpdatableByAdmin(command);

		auctionVo.updateByAdmin(command.startPrice(), command.currentPrice(), command.endTime());

		Map<String, Object> auctionVoMap = objectMapper.convertValue(auctionVo, new TypeReference<>() {
		});
		redisTemplate.opsForHash().putAll(auctionKey, auctionVoMap);

		if (command.endTime() != null) {
			long score = command.endTime().atZone(ZoneId.of("Asia/Seoul")).toEpochSecond();
			redisTemplate.opsForZSet().add(AUCTION_ENDING_PREFIX, auctionId, score);
		}

		auctionAdminDomainService.updateAuction(auctionId, command);
	}

	private void acquireLockOrThrow(RLock auctionLock) {
		if (!auctionLock.tryLock()) {
			throw new AuctionException(AuctionErrorCode.AUCTION_PROCESSING_BUSY);
		}
	}
}
