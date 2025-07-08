package nbc.chillguys.nebulazone.application.auction.service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.test.util.ReflectionTestUtils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import nbc.chillguys.nebulazone.application.auction.dto.request.ManualEndAuctionRequest;
import nbc.chillguys.nebulazone.application.auction.dto.response.DeleteAuctionResponse;
import nbc.chillguys.nebulazone.application.auction.dto.response.EndAuctionResponse;
import nbc.chillguys.nebulazone.application.auction.dto.response.FindSortTypeAuctionResponse;
import nbc.chillguys.nebulazone.domain.auction.entity.Auction;
import nbc.chillguys.nebulazone.domain.auction.entity.AuctionSortType;
import nbc.chillguys.nebulazone.domain.auction.exception.AuctionErrorCode;
import nbc.chillguys.nebulazone.domain.auction.exception.AuctionException;
import nbc.chillguys.nebulazone.domain.auction.service.AuctionDomainService;
import nbc.chillguys.nebulazone.domain.bid.entity.BidStatus;
import nbc.chillguys.nebulazone.domain.bid.exception.BidErrorCode;
import nbc.chillguys.nebulazone.domain.bid.exception.BidException;
import nbc.chillguys.nebulazone.domain.bid.service.BidDomainService;
import nbc.chillguys.nebulazone.domain.product.entity.Product;
import nbc.chillguys.nebulazone.domain.product.entity.ProductEndTime;
import nbc.chillguys.nebulazone.domain.product.entity.ProductTxMethod;
import nbc.chillguys.nebulazone.domain.product.event.ProductDeletedEvent;
import nbc.chillguys.nebulazone.domain.product.service.ProductDomainService;
import nbc.chillguys.nebulazone.domain.transaction.service.TransactionDomainService;
import nbc.chillguys.nebulazone.domain.user.entity.User;
import nbc.chillguys.nebulazone.domain.user.service.UserDomainService;
import nbc.chillguys.nebulazone.infra.redis.dto.CreateRedisAuctionDto;
import nbc.chillguys.nebulazone.infra.redis.dto.FindAllAuctionsDto;
import nbc.chillguys.nebulazone.infra.redis.publisher.RedisMessagePublisher;
import nbc.chillguys.nebulazone.infra.redis.service.UserCacheService;
import nbc.chillguys.nebulazone.infra.redis.vo.AuctionVo;
import nbc.chillguys.nebulazone.infra.redis.vo.BidVo;

@DisplayName("경매 레디스 서비스 단위 테스트")
@ExtendWith(MockitoExtension.class)
class AuctionRedisServiceTest {

	@Mock
	private RedisTemplate<String, Object> redisTemplate;

	@Mock
	private HashOperations<String, Object, Object> hashOperations;

	@Mock
	private ValueOperations<String, Object> valueOperations;

	@Mock
	private ZSetOperations<String, Object> zSetOperations;

	@Mock
	private ApplicationEventPublisher eventPublisher;

	@Mock
	private ObjectMapper objectMapper;

	@Mock
	private AuctionDomainService auctionDomainService;

	@Mock
	private UserDomainService userDomainService;

	@Mock
	private BidDomainService bidDomainService;

	@Mock
	private TransactionDomainService transactionDomainService;

	@Mock
	private ProductDomainService productDomainService;

	@Mock
	private UserCacheService userCacheService;

	@Mock
	private RedisMessagePublisher redisMessagePublisher;

	@InjectMocks
	private AuctionRedisService auctionRedisService;

	private User user;
	private AuctionVo auctionVo;
	private AuctionVo closedAuctionVo;
	private AuctionVo wonAuctionVo;
	private BidVo bidVo;
	private CreateRedisAuctionDto createRedisAuctionDto;
	private Product product;
	private Auction auction;

	@BeforeEach
	void init() {
		user = User.builder()
			.email("seller@test.com")
			.nickname("판매자닉네임")
			.build();
		ReflectionTestUtils.setField(user, "id", 1L);

		auctionVo = new AuctionVo(
			1L, 100000L, 120000L, LocalDateTime.now().plusDays(1), false, LocalDateTime.now(),
			1L, "테스트 상품", false, 1L, "판매자닉네임", "seller@test.com", List.of("image1.jpg"));

		closedAuctionVo = new AuctionVo(
			1L, 100000L, 120000L, LocalDateTime.now().minusDays(1), false, LocalDateTime.now(),
			1L, "테스트 상품", false, 1L, "판매자닉네임", "seller@test.com", List.of("image1.jpg"));

		wonAuctionVo = new AuctionVo(
			1L, 100000L, 120000L, LocalDateTime.now().plusDays(1), true, LocalDateTime.now(),
			1L, "테스트 상품", false, 1L, "판매자닉네임", "seller@test.com", List.of("image1.jpg"));

		bidVo = new BidVo(
			"bid-uuid-123",
			2L,
			"입찰자닉네임",
			"bidder@test.com",
			120000L,
			1L,
			BidStatus.WON.name(),
			LocalDateTime.now());

		product = Product.builder()
			.name("테스트 상품")
			.description("테스트 상품 설명")
			.price(100000L)
			.txMethod(ProductTxMethod.AUCTION)
			.seller(user)
			.catalog(null)
			.build();
		ReflectionTestUtils.setField(product, "id", 1L);

		auction = Auction.builder()
			.startPrice(100000L)
			.currentPrice(100000L)
			.product(product)
			.build();
		ReflectionTestUtils.setField(auction, "id", 1L);

		createRedisAuctionDto = CreateRedisAuctionDto.of(product, auction, user, ProductEndTime.HOUR_24);
	}

	@Nested
	@DisplayName("경매 생성 테스트")
	class CreateAuctionTest {

		@DisplayName("레디스 경매 생성 성공")
		@Test
		void success_createAuction() {
			// given
			given(redisTemplate.opsForHash()).willReturn(hashOperations);
			given(redisTemplate.opsForZSet()).willReturn(zSetOperations);

			Map<String, Object> auctionDataMap = Map.of("auctionId", 1L);
			doReturn(auctionDataMap).when(objectMapper)
				.convertValue(any(AuctionVo.class), any(TypeReference.class));

			willDoNothing().given(hashOperations).putAll(anyString(), anyMap());
			given(zSetOperations.add(anyString(), any(), anyDouble())).willReturn(true);

			// when
			assertDoesNotThrow(() -> auctionRedisService.createAuction(createRedisAuctionDto));

			// then
			verify(hashOperations, times(1))
				.putAll(eq("auction:" + auction.getId()), eq(auctionDataMap));
			verify(zSetOperations, times(1))
				.add(eq("auction:ending"), eq(auction.getId()), anyDouble());
		}
	}

	@Nested
	@DisplayName("경매 수동 낙찰 테스트")
	class ManualEndAuctionTest {

		@DisplayName("수동 낙찰 성공")
		@Test
		void success_manualEndAuction() {
			// given
			Long auctionId = 1L;
			ManualEndAuctionRequest request = new ManualEndAuctionRequest(120000L, "입찰자닉네임");

			given(redisTemplate.opsForHash()).willReturn(hashOperations);
			given(redisTemplate.opsForZSet()).willReturn(zSetOperations);
			given(hashOperations.entries("auction:" + auctionId))
				.willReturn(Map.of("auctionId", "1"));
			given(objectMapper.convertValue(any(), eq(AuctionVo.class))).willReturn(auctionVo);
			given(auctionDomainService.manualEndAuction(auctionId, 120000L)).willReturn(auction);

			Map<String, Object> bidData = Map.of(
				"bidUuid", "bid-uuid-123",
				"bidUserId", 2L,
				"bidUserNickname", "입찰자닉네임",
				"bidUserEmail", "bidder@test.com",
				"bidPrice", 120000L,
				"auctionId", 1L,
				"bidStatus", BidStatus.WON.name(),
				"bidCreatedAt", LocalDateTime.now().toString()
			);

			given(zSetOperations.range(anyString(), anyLong(), anyLong())).willReturn(Set.of(bidData));
			given(objectMapper.convertValue(eq(bidData), eq(BidVo.class))).willReturn(bidVo);

			User bidder = User.builder()
				.email("bidder@test.com")
				.nickname("입찰자닉네임")
				.build();
			ReflectionTestUtils.setField(bidder, "id", 2L);
			given(userDomainService.findActiveUserByIds(anyList())).willReturn(List.of(bidder));

			willDoNothing().given(bidDomainService).createAllBid(any(), anyList(), anyMap());
			given(transactionDomainService.createTransaction(any())).willReturn(null);
			willDoNothing().given(productDomainService).markProductAsPurchased(anyLong());
			willDoNothing().given(userCacheService).deleteUserById(anyLong());
			given(zSetOperations.remove(anyString(), any())).willReturn(1L);
			given(redisTemplate.delete(anyString())).willReturn(true);
			willDoNothing().given(redisMessagePublisher).publishAuctionUpdate(anyLong(), anyString(), any());

			// when
			EndAuctionResponse result = auctionRedisService.manualEndAuction(auctionId, user, request);

			// then
			assertThat(result.auctionId()).isEqualTo(auctionId);
		}

		@DisplayName("수동 낙찰 실패 - 경매 생성자가 아님")
		@Test
		void fail_manualEndAuction_notAuctionOwner() {
			// given
			Long auctionId = 1L;
			ManualEndAuctionRequest request = new ManualEndAuctionRequest(120000L, "판매자닉네임");
			User otherUser = User.builder()
				.email("other@test.com")
				.nickname("다른사용자")
				.build();
			ReflectionTestUtils.setField(otherUser, "id", 2L);

			given(redisTemplate.opsForHash()).willReturn(hashOperations);
			given(hashOperations.entries("auction:" + auctionId))
				.willReturn(Map.of("auctionId", "1"));
			given(objectMapper.convertValue(any(), eq(AuctionVo.class))).willReturn(auctionVo);

			// when & then
			assertThatThrownBy(() -> auctionRedisService.manualEndAuction(auctionId, otherUser, request))
				.isInstanceOf(AuctionException.class)
				.hasFieldOrPropertyWithValue("errorCode", AuctionErrorCode.AUCTION_NOT_OWNER);
		}

		@DisplayName("수동 낙찰 실패 - 낙찰 요청 입찰가와 경매에 등록된 입찰가가 다름")
		@Test
		void fail_manualEndAuction_mismatchBidPrice() {
			// given
			Long auctionId = 1L;
			ManualEndAuctionRequest request = new ManualEndAuctionRequest(130000L, "입찰자닉네임");

			given(redisTemplate.opsForHash()).willReturn(hashOperations);
			given(hashOperations.entries("auction:" + auctionId))
				.willReturn(Map.of("auctionId", "1"));
			given(objectMapper.convertValue(any(), eq(AuctionVo.class))).willReturn(auctionVo);

			// when & then
			assertThatThrownBy(() -> auctionRedisService.manualEndAuction(auctionId, user, request))
				.isInstanceOf(AuctionException.class)
				.hasFieldOrPropertyWithValue("errorCode", AuctionErrorCode.MISMATCH_BID_PRICE);
		}

		@DisplayName("수동 낙찰 실패 - 종료된 경매")
		@Test
		void fail_manualEndAuction_closed() {
			// given
			Long auctionId = 1L;
			ManualEndAuctionRequest request = new ManualEndAuctionRequest(120000L, "입찰자닉네임");

			given(redisTemplate.opsForHash()).willReturn(hashOperations);
			given(hashOperations.entries("auction:" + auctionId))
				.willReturn(Map.of("auctionId", "1"));
			given(objectMapper.convertValue(any(), eq(AuctionVo.class))).willReturn(closedAuctionVo);

			// when & then
			assertThatThrownBy(() -> auctionRedisService.manualEndAuction(auctionId, user, request))
				.isInstanceOf(AuctionException.class)
				.hasFieldOrPropertyWithValue("errorCode", AuctionErrorCode.ALREADY_CLOSED_AUCTION);
		}

		@DisplayName("수동 낙찰 실패 - 낙찰된 경매")
		@Test
		void fail_manualEndAuction_won() {
			// given
			Long auctionId = 1L;
			ManualEndAuctionRequest request = new ManualEndAuctionRequest(120000L, "입찰자닉네임");

			given(redisTemplate.opsForHash()).willReturn(hashOperations);
			given(hashOperations.entries("auction:" + auctionId))
				.willReturn(Map.of("auctionId", "1"));
			given(objectMapper.convertValue(any(), eq(AuctionVo.class))).willReturn(wonAuctionVo);

			// when & then
			assertThatThrownBy(() -> auctionRedisService.manualEndAuction(auctionId, user, request))
				.isInstanceOf(AuctionException.class)
				.hasFieldOrPropertyWithValue("errorCode", AuctionErrorCode.ALREADY_WON_AUCTION);
		}

		@DisplayName("수동 낙찰 실패 - 경매에 등록된 입찰 유저와 낙찰 시 요청된 입찰 유저가 다름")
		@Test
		void fail_manualEndAuction_mismatchBidUser() {
			// given
			Long auctionId = 1L;
			ManualEndAuctionRequest request = new ManualEndAuctionRequest(120000L, "다른입찰자");

			given(redisTemplate.opsForHash()).willReturn(hashOperations);
			given(redisTemplate.opsForZSet()).willReturn(zSetOperations);
			given(hashOperations.entries("auction:" + auctionId))
				.willReturn(Map.of("auctionId", "1"));
			given(objectMapper.convertValue(any(), eq(AuctionVo.class))).willReturn(auctionVo);
			given(auctionDomainService.manualEndAuction(auctionId, 120000L)).willReturn(auction);
			given(zSetOperations.range(anyString(), anyLong(), anyLong())).willReturn(Set.of());

			// when & then
			assertThatThrownBy(() -> auctionRedisService.manualEndAuction(auctionId, user, request))
				.isInstanceOf(BidException.class)
				.hasFieldOrPropertyWithValue("errorCode", BidErrorCode.BID_NOT_FOUND);
		}
	}

	@Nested
	@DisplayName("경매 삭제 테스트")
	class DeleteAuctionTest {

		@DisplayName("경매 삭제 성공")
		@Test
		void success_deleteAuction() {
			// given
			Long auctionId = 1L;

			given(redisTemplate.opsForHash()).willReturn(hashOperations);
			given(redisTemplate.opsForZSet()).willReturn(zSetOperations);
			given(hashOperations.entries("auction:" + auctionId))
				.willReturn(Map.of("auctionId", "1", "productId", "1"));
			given(objectMapper.convertValue(any(), eq(AuctionVo.class))).willReturn(auctionVo);
			given(auctionDomainService.deleteAuction(auctionId)).willReturn(auction);
			given(zSetOperations.range(anyString(), anyLong(), anyLong())).willReturn(Set.of());
			given(userDomainService.findActiveUserByIds(anyList())).willReturn(List.of());
			willDoNothing().given(bidDomainService).createAllBid(any(), anyList(), anyMap());
			given(zSetOperations.remove(anyString(), any())).willReturn(1L);
			given(redisTemplate.delete(anyString())).willReturn(true);
			willDoNothing().given(redisMessagePublisher).publishAuctionUpdate(anyLong(), anyString(), any());
			willDoNothing().given(eventPublisher).publishEvent(any(ProductDeletedEvent.class));

			// when
			DeleteAuctionResponse result = auctionRedisService.deleteAuction(auctionId, user);

			// then
			assertThat(result.auctionId()).isEqualTo(auctionId);
			assertThat(result.productId()).isEqualTo(1L);
		}

		@DisplayName("경매 삭제 실패 - 경매 생성자가 아님")
		@Test
		void fail_deleteAuction_notAuctionOwner() {
			// given
			Long auctionId = 1L;
			User otherUser = User.builder()
				.email("other@test.com")
				.nickname("다른사용자")
				.build();
			ReflectionTestUtils.setField(otherUser, "id", 2L);

			given(redisTemplate.opsForHash()).willReturn(hashOperations);
			given(hashOperations.entries("auction:" + auctionId))
				.willReturn(Map.of("auctionId", "1"));
			given(objectMapper.convertValue(any(), eq(AuctionVo.class))).willReturn(auctionVo);

			// when & then
			assertThatThrownBy(() -> auctionRedisService.deleteAuction(auctionId, otherUser))
				.isInstanceOf(AuctionException.class)
				.hasFieldOrPropertyWithValue("errorCode", AuctionErrorCode.AUCTION_NOT_OWNER);
		}
	}

	@Nested
	@DisplayName("정렬 기반 경매 조회 테스트")
	class FindAuctionSortTypeTest {

		@DisplayName("정렬 기반 경매 조회 성공 - 마감 임박순")
		@Test
		void success_findAuctionsBySortType_closing() throws Exception {
			// given
			given(redisTemplate.opsForValue()).willReturn(valueOperations);
			given(valueOperations.get("auction:sorted:CLOSING"))
				.willReturn("cached_response");
			FindSortTypeAuctionResponse mockResponse = FindSortTypeAuctionResponse.from(List.of(
				new FindAllAuctionsDto(1L, 100000L, 120000L, false,
					LocalDateTime.now().plusDays(1), LocalDateTime.now(), 1L, "경매상품1", "image1.jpg", 3L)
			));
			given(objectMapper.convertValue(eq("cached_response"), eq(FindSortTypeAuctionResponse.class)))
				.willReturn(mockResponse);

			// when
			FindSortTypeAuctionResponse result = auctionRedisService.findAuctionsBySortType(
				AuctionSortType.CLOSING);

			// then
			assertThat(result.auctions()).hasSize(1);
		}

		@DisplayName("정렬 기반 경매 조회 성공 - 인기순(입찰개수)")
		@Test
		void success_findAuctionsBySortType_popular() throws Exception {
			// given
			given(redisTemplate.opsForValue()).willReturn(valueOperations);
			given(valueOperations.get("auction:sorted:POPULAR"))
				.willReturn("cached_response");
			FindSortTypeAuctionResponse mockResponse = FindSortTypeAuctionResponse.from(List.of(
				new FindAllAuctionsDto(1L, 100000L, 120000L, false,
					LocalDateTime.now().plusDays(1), LocalDateTime.now(), 1L, "인기경매상품1", "image1.jpg", 10L)
			));
			given(objectMapper.convertValue(eq("cached_response"), eq(FindSortTypeAuctionResponse.class)))
				.willReturn(mockResponse);

			// when
			FindSortTypeAuctionResponse result = auctionRedisService.findAuctionsBySortType(
				AuctionSortType.POPULAR);

			// then
			assertThat(result.auctions()).hasSize(1);
		}
	}

	@Nested
	@DisplayName("레디스에 저장된 특정 경매 조회 테스트 - api 요청시 사용")
	class GetAuctionVoElseThrowTest {

		@DisplayName("조회 성공")
		@Test
		void success_getAuctionVoElseThrow() {
			// given
			Long auctionId = 1L;

			given(redisTemplate.opsForHash()).willReturn(hashOperations);
			given(hashOperations.entries("auction:" + auctionId))
				.willReturn(Map.of("auctionId", "1"));
			given(objectMapper.convertValue(any(), eq(AuctionVo.class))).willReturn(auctionVo);

			// when
			AuctionVo result = auctionRedisService.getAuctionVoElseThrow(auctionId);

			// then
			assertThat(result).isEqualTo(auctionVo);
		}

		@DisplayName("조회 실패 - 레디스에 없음")
		@Test
		void fail_getAuctionVoElseThrow_notFound() {
			// given
			Long auctionId = 999L;

			given(redisTemplate.opsForHash()).willReturn(hashOperations);
			given(hashOperations.entries("auction:" + auctionId))
				.willReturn(Map.of());

			// when & then
			assertThatThrownBy(() -> auctionRedisService.getAuctionVoElseThrow(auctionId))
				.isInstanceOf(AuctionException.class)
				.hasFieldOrPropertyWithValue("errorCode", AuctionErrorCode.AUCTION_NOT_FOUND);
		}
	}

	@Nested
	@DisplayName("레디스에 저장된 특정 경매 조회 테스트 - 자동 로직 동작시 사용")
	class FindRedisAuctionVoTest {

		@DisplayName("조회 성공")
		@Test
		void success_findRedisAuctionVo() {
			// given
			Long auctionId = 1L;

			given(redisTemplate.opsForHash()).willReturn(hashOperations);
			given(hashOperations.entries("auction:" + auctionId))
				.willReturn(Map.of("auctionId", "1"));
			given(objectMapper.convertValue(any(), eq(AuctionVo.class))).willReturn(auctionVo);

			// when
			AuctionVo result = auctionRedisService.findRedisAuctionVo(auctionId);

			// then
			assertThat(result).isEqualTo(auctionVo);
		}

		@DisplayName("조회 실패 - 레디스에 없음")
		@Test
		void success_findRedisAuctionVo_notFound() {
			// given
			Long auctionId = 999L;

			given(redisTemplate.opsForHash()).willReturn(hashOperations);
			given(hashOperations.entries("auction:" + auctionId))
				.willReturn(Map.of());

			// when
			AuctionVo result = auctionRedisService.findRedisAuctionVo(auctionId);

			// then
			assertThat(result).isNull();
		}
	}

	@Nested
	@DisplayName("특정 경매의 입찰 최고가 갱신 테스트")
	class UpdateAuctionCurrentPriceTest {

		@DisplayName("갱신 성공")
		@Test
		void success_updateAuctionCurrentPrice() {
			// given
			Long auctionId = 1L;
			Long newPrice = 150000L;

			given(redisTemplate.opsForHash()).willReturn(hashOperations);
			willDoNothing().given(hashOperations).put(anyString(), anyString(), any());

			// when
			auctionRedisService.updateAuctionCurrentPrice(auctionId, newPrice);

			// then
			verify(hashOperations, times(1)).put("auction:" + auctionId, "currentPrice", newPrice);
		}
	}

	@Nested
	@DisplayName("레디스 전체 경매의 id를 조회 테스트")
	class FindAllAuctionIdsTest {

		@DisplayName("조회 성공")
		@Test
		void success_findAllAuctionVoIds() {
			// given
			given(redisTemplate.opsForZSet()).willReturn(zSetOperations);
			given(zSetOperations.range(anyString(), anyLong(), anyLong()))
				.willReturn(Set.of(1L, 2L, 3L));

			// when
			List<Long> result = auctionRedisService.findAllAuctionVoIds();

			// then
			assertThat(result).hasSize(3);
			assertThat(result).contains(1L, 2L, 3L);
		}

		@DisplayName("조회 성공 - 0건")
		@Test
		void success_findAllAuctionVoIds_empty() {
			// given
			given(redisTemplate.opsForZSet()).willReturn(zSetOperations);
			given(zSetOperations.range(anyString(), anyLong(), anyLong()))
				.willReturn(Set.of());

			// when
			List<Long> result = auctionRedisService.findAllAuctionVoIds();

			// then
			assertThat(result).isEmpty();
		}
	}

	@Nested
	@DisplayName("레디스 경매 데이터의 이미지 url 수정")
	class UpdateAuctionProductImagesTest {

		@DisplayName("수정 성공")
		@Test
		void success_updateAuctionProductImages() {
			// given
			Long auctionId = 1L;
			List<String> imageUrls = List.of("image1.jpg", "image2.jpg");

			given(redisTemplate.opsForHash()).willReturn(hashOperations);
			willDoNothing().given(hashOperations).put(anyString(), anyString(), any());

			// when
			auctionRedisService.updateAuctionProductImages(auctionId, imageUrls);

			// then
			verify(hashOperations, times(1)).put("auction:" + auctionId, "productImageUrls", imageUrls);
		}
	}
}
