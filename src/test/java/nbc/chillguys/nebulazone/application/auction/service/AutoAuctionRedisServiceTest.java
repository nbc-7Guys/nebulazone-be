package nbc.chillguys.nebulazone.application.auction.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;

import com.fasterxml.jackson.databind.ObjectMapper;

import nbc.chillguys.nebulazone.application.auction.dto.response.EndAuctionResponse;
import nbc.chillguys.nebulazone.domain.auction.entity.Auction;
import nbc.chillguys.nebulazone.domain.auction.service.AutoAuctionDomainService;
import nbc.chillguys.nebulazone.domain.bid.entity.BidStatus;
import nbc.chillguys.nebulazone.domain.bid.service.BidDomainService;
import nbc.chillguys.nebulazone.domain.product.entity.Product;
import nbc.chillguys.nebulazone.domain.product.entity.ProductTxMethod;
import nbc.chillguys.nebulazone.domain.product.service.ProductDomainService;
import nbc.chillguys.nebulazone.domain.transaction.dto.TransactionCreateCommand;
import nbc.chillguys.nebulazone.domain.transaction.entity.Transaction;
import nbc.chillguys.nebulazone.domain.transaction.service.TransactionDomainService;
import nbc.chillguys.nebulazone.domain.user.entity.User;
import nbc.chillguys.nebulazone.domain.user.service.UserDomainService;
import nbc.chillguys.nebulazone.infra.redis.publisher.RedisMessagePublisher;
import nbc.chillguys.nebulazone.infra.redis.service.UserCacheService;
import nbc.chillguys.nebulazone.infra.redis.vo.AuctionVo;
import nbc.chillguys.nebulazone.infra.redis.vo.BidVo;

@DisplayName("자동 경매 서비스 단위 테스트")
@ExtendWith(MockitoExtension.class)
class AutoAuctionRedisServiceTest {

	private RedisTemplate<String, Object> redisTemplate;
	private HashOperations<String, Object, Object> hashOperations;
	private ZSetOperations<String, Object> zSetOperations;
	private ObjectMapper objectMapper;
	private AutoAuctionDomainService autoAuctionDomainService;
	private BidDomainService bidDomainService;
	private UserDomainService userDomainService;
	private TransactionDomainService transactionDomainService;
	private ProductDomainService productDomainService;
	private UserCacheService userCacheService;
	private RedisMessagePublisher messagePublisher;
	private AutoAuctionRedisService autoAuctionRedisService;

	private AuctionVo auctionVo;
	private Auction mockAuction;
	private Product mockProduct;
	private User mockSeller;
	private User mockBidder;
	private Transaction mockTransaction;
	private BidVo mockBidVo;

	@BeforeEach
	void init() {
		redisTemplate = mock(RedisTemplate.class);
		hashOperations = mock(HashOperations.class);
		zSetOperations = mock(ZSetOperations.class);
		objectMapper = mock(ObjectMapper.class);
		autoAuctionDomainService = mock(AutoAuctionDomainService.class);
		bidDomainService = mock(BidDomainService.class);
		userDomainService = mock(UserDomainService.class);
		transactionDomainService = mock(TransactionDomainService.class);
		productDomainService = mock(ProductDomainService.class);
		userCacheService = mock(UserCacheService.class);
		messagePublisher = mock(RedisMessagePublisher.class);

		autoAuctionRedisService = new AutoAuctionRedisService(
			redisTemplate, objectMapper, autoAuctionDomainService, bidDomainService,
			userDomainService, transactionDomainService, productDomainService,
			userCacheService, messagePublisher
		);

		auctionVo = new AuctionVo(
			1L, 100000L, 120000L, LocalDateTime.now().minusMinutes(1), false, LocalDateTime.now(),
			1L, "테스트 상품", false, 1L, "판매자닉네임", "seller@test.com", List.of("image1.jpg"));

		mockAuction = mock(Auction.class);
		mockProduct = mock(Product.class);
		mockSeller = mock(User.class);
		mockBidder = mock(User.class);
		mockTransaction = mock(Transaction.class);
		mockBidVo = mock(BidVo.class);
	}

	@Nested
	@DisplayName("경매 자동낙찰 테스트")
	class AutoEndAuction {

		@DisplayName("자동낙찰 성공 - 유찰된 경우")
		@Test
		void success_processAuctionEnding_unSold() {
			// given
			Long auctionId = 1L;
			long currentPrice = 120000L;
			String auctionKey = "auction:" + auctionId;
			String bidHistoryKey = "bid:auction:" + auctionId;

			given(redisTemplate.opsForHash()).willReturn(hashOperations);
			given(redisTemplate.opsForZSet()).willReturn(zSetOperations);
			given(hashOperations.entries(auctionKey))
				.willReturn(Map.of("auctionId", "1", "currentPrice", String.valueOf(currentPrice)));

			doAnswer(new Answer<AuctionVo>() {
				@Override
				public AuctionVo answer(InvocationOnMock invocation) throws Throwable {

					Map<Object, Object> auctionMap = invocation.getArgument(0);
					return new AuctionVo(
						Long.parseLong((String)auctionMap.get("auctionId")),
						Long.parseLong((String)auctionMap.get("currentPrice")),
						120000L,
						LocalDateTime.now().minusMinutes(1),
						false,
						LocalDateTime.now(),
						1L,
						"테스트 상품",
						false,
						1L,
						"판매자닉네임",
						"seller@test.com",
						List.of("image1.jpg")
					);
				}
			}).when(objectMapper).convertValue(anyMap(), eq(AuctionVo.class));

			given(zSetOperations.range(bidHistoryKey, 0, -1)).willReturn(Collections.emptySet());
			given(autoAuctionDomainService.autoEndAuction(auctionId, currentPrice)).willReturn(null);

			// when
			assertDoesNotThrow(() -> autoAuctionRedisService.processAuctionEnding(auctionId));

			// then
			verify(autoAuctionDomainService, times(1)).autoEndAuction(auctionId, currentPrice);
			verify(zSetOperations, times(1)).remove(eq("auction:ending"), eq(auctionId));
			verify(redisTemplate, times(1)).delete(eq(auctionKey));
			verify(redisTemplate, times(1)).delete(eq(bidHistoryKey));
			verify(transactionDomainService, never()).createTransaction(any());
			verify(messagePublisher, never()).publishAuctionUpdate(anyLong(), anyString(), any());
			verify(productDomainService, never()).markProductAsPurchased(anyLong());
			verify(userCacheService, never()).deleteUserById(anyLong());
		}

		@DisplayName("자동낙찰 성공 - 낙찰된 경우")
		@Test
		void success_processAuctionEnding_won() {
			// given
			Long auctionId = 1L;
			long currentPrice = 120000L;
			Long sellerId = 2L;
			Long bidderId = 3L;
			String auctionKey = "auction:" + auctionId;
			String bidHistoryKey = "bid:auction:" + auctionId;

			given(redisTemplate.opsForHash()).willReturn(hashOperations);
			given(redisTemplate.opsForZSet()).willReturn(zSetOperations);
			given(hashOperations.entries(auctionKey))
				.willReturn(Map.of("auctionId", "1", "currentPrice", String.valueOf(currentPrice)));
			given(objectMapper.convertValue(anyMap(), eq(AuctionVo.class))).willReturn(auctionVo);

			given(autoAuctionDomainService.autoEndAuction(auctionId, currentPrice)).willReturn(mockAuction);
			given(mockAuction.isWon()).willReturn(true);
			given(mockAuction.getCurrentPrice()).willReturn(currentPrice);
			given(mockAuction.getProduct()).willReturn(mockProduct);

			given(mockProduct.getId()).willReturn(1L);
			given(mockProduct.getSeller()).willReturn(mockSeller);
			given(mockProduct.getTxMethod()).willReturn(ProductTxMethod.AUCTION);
			given(mockSeller.getId()).willReturn(sellerId);

			Map<String, Object> rawBidData = Map.of(
				"bidUuid", "bid-uuid-123",
				"bidUserId", bidderId,
				"bidUserNickname", "입찰자닉네임",
				"bidUserEmail", "bidder@test.com",
				"bidPrice", currentPrice,
				"auctionId", auctionId,
				"bidStatus", BidStatus.BID.name(),
				"bidCreatedAt", LocalDateTime.now().toString()
			);
			given(zSetOperations.range(bidHistoryKey, 0, -1)).willReturn(Set.of(rawBidData));

			given(objectMapper.convertValue(any(Map.class), eq(BidVo.class))).willReturn(mockBidVo);

			given(mockBidVo.getBidPrice()).willReturn(currentPrice);
			given(mockBidVo.getBidUserId()).willReturn(bidderId);
			given(mockBidVo.getBidStatus()).willReturn(BidStatus.BID.name());

			doAnswer(invocation -> {
				given(mockBidVo.getBidStatus()).willReturn(BidStatus.WON.name());
				return null;
			}).when(mockBidVo).wonBid();

			given(mockBidder.getId()).willReturn(bidderId);
			given(userDomainService.findActiveUserByIds(anyList())).willReturn(List.of(mockBidder));
			given(transactionDomainService.createTransaction(any())).willReturn(mockTransaction);
			doNothing().when(messagePublisher).publishAuctionUpdate(anyLong(), anyString(), any());

			// when
			assertDoesNotThrow(() -> autoAuctionRedisService.processAuctionEnding(auctionId));

			// then
			verify(autoAuctionDomainService, times(1)).autoEndAuction(auctionId, currentPrice);

			verify(mockBidVo, times(1)).wonBid();

			verify(mockProduct, times(1)).purchase();
			verify(productDomainService, times(1)).markProductAsPurchased(1L);
			verify(mockSeller, times(1)).plusPoint(currentPrice);
			verify(userCacheService, times(1)).deleteUserById(sellerId);

			ArgumentCaptor<List> bidVoListCaptor = ArgumentCaptor.forClass(List.class);
			ArgumentCaptor<Map> userMapCaptor = ArgumentCaptor.forClass(Map.class);
			verify(bidDomainService, times(1)).createAllBid(eq(mockAuction), bidVoListCaptor.capture(),
				userMapCaptor.capture());
			// Assertions for captured bidVoList and userMap can be added here if needed

			ArgumentCaptor<TransactionCreateCommand> transactionCaptor = ArgumentCaptor.forClass(
				TransactionCreateCommand.class);
			verify(transactionDomainService, times(2)).createTransaction(transactionCaptor.capture());
			// Assertions for captured transactions can be added here if needed

			verify(messagePublisher, times(1)).publishAuctionUpdate(eq(auctionId), eq("won"),
				any(EndAuctionResponse.class));
			verify(messagePublisher, times(1)).publishAuctionUpdate(eq(auctionId), eq("bid"),
				any(EndAuctionResponse.class));

			verify(zSetOperations, times(1)).remove(eq("auction:ending"), eq(auctionId));
			verify(redisTemplate, times(1)).delete(eq(auctionKey));
			verify(redisTemplate, times(1)).delete(eq(bidHistoryKey));
		}
	}
}
