package nbc.chillguys.nebulazone.application.bid.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;

import com.fasterxml.jackson.databind.ObjectMapper;

import nbc.chillguys.nebulazone.application.auction.service.AuctionRedisService;
import nbc.chillguys.nebulazone.application.bid.dto.response.CreateBidResponse;
import nbc.chillguys.nebulazone.application.bid.dto.response.DeleteBidResponse;
import nbc.chillguys.nebulazone.application.bid.dto.response.FindBidResponse;
import nbc.chillguys.nebulazone.application.bid.metrics.BidMetrics;
import nbc.chillguys.nebulazone.domain.auction.exception.AuctionErrorCode;
import nbc.chillguys.nebulazone.domain.auction.exception.AuctionException;
import nbc.chillguys.nebulazone.domain.bid.entity.BidStatus;
import nbc.chillguys.nebulazone.domain.bid.exception.BidErrorCode;
import nbc.chillguys.nebulazone.domain.bid.exception.BidException;
import nbc.chillguys.nebulazone.domain.user.entity.User;
import nbc.chillguys.nebulazone.domain.user.service.UserDomainService;
import nbc.chillguys.nebulazone.infra.redis.publisher.RedisMessagePublisher;
import nbc.chillguys.nebulazone.infra.redis.service.UserCacheService;
import nbc.chillguys.nebulazone.infra.redis.vo.AuctionVo;
import nbc.chillguys.nebulazone.infra.redis.vo.BidVo;

@DisplayName("입찰 레디스 서비스 단위 테스트")
@ExtendWith(MockitoExtension.class)
class BidRedisServiceTest {

	@Mock
	private RedisTemplate<String, Object> redisTemplate;
	@Mock
	private ObjectMapper objectMapper;
	@Mock
	private UserDomainService userDomainService;
	@Mock
	private UserCacheService userCacheService;
	@Mock
	private AuctionRedisService auctionRedisService;
	@Mock
	private RedisMessagePublisher redisMessagePublisher;
	@Mock
	private BidMetrics bidMetrics;
	@Mock
	private ZSetOperations<String, Object> zSetOperations;

	@InjectMocks
	private BidRedisService bidRedisService;

	@DisplayName("입찰 생성 테스트")
	@Nested
	class CreatedBidTest {

		@DisplayName("입찰 생성 성공")
		@Test
		void success_createBid() {
			// given
			Long auctionId = 1L;
			Long userId = 100L;
			Long bidPrice = 1000L;

			User mockUser = mock(User.class);
			given(mockUser.getId()).willReturn(userId);

			AuctionVo mockAuctionVo = mock(AuctionVo.class);

			given(userDomainService.findActiveUserById(userId)).willReturn(mockUser);
			given(auctionRedisService.getAuctionVoElseThrow(auctionId)).willReturn(mockAuctionVo);
			given(redisTemplate.opsForZSet()).willReturn(zSetOperations);
			given(zSetOperations.range(anyString(), anyLong(), anyLong())).willReturn(Collections.emptySet());

			// when
			CreateBidResponse response = bidRedisService.createBid(auctionId, mockUser, bidPrice);

			// then
			assertThat(response.auctionId()).isEqualTo(auctionId);
			assertThat(response.bidPrice()).isEqualTo(bidPrice);
			assertThat(response.bidTime()).isNotNull();
			assertThat(response.bidTime()).isBefore(LocalDateTime.now().plusSeconds(1));

			then(userDomainService).should().findActiveUserById(userId);
			then(auctionRedisService).should().getAuctionVoElseThrow(auctionId);
			then(mockAuctionVo).should().validAuctionNotClosed();
			then(mockAuctionVo).should().validWonAuction();
			then(mockAuctionVo).should().validAuctionOwnerNotBid(userId);
			then(mockAuctionVo).should().validMinimumBidPrice(bidPrice);
			then(zSetOperations).should().range(eq("bid:auction:" + auctionId), eq(0L), eq(-1L));
			then(zSetOperations).should().add(eq("bid:auction:" + auctionId), any(BidVo.class), eq((double)bidPrice));
			then(mockUser).should().minusPoint(bidPrice);
			then(userCacheService).should().deleteUserById(userId);
			then(auctionRedisService).should().updateAuctionCurrentPrice(auctionId, bidPrice);
			then(redisMessagePublisher).should()
				.publishAuctionUpdate(eq(auctionId), eq("bid"), any(CreateBidResponse.class));
		}

		@DisplayName("입찰 생성 실패 - 종료된 경매")
		@Test
		void fail_createBid_auctionClosed() {
			// given
			Long auctionId = 1L;
			Long userId = 100L;
			Long bidPrice = 1000L;
			User mockUser = mock(User.class);
			given(mockUser.getId()).willReturn(userId);
			AuctionVo mockAuctionVo = mock(AuctionVo.class);

			given(userDomainService.findActiveUserById(userId)).willReturn(mockUser);
			given(auctionRedisService.getAuctionVoElseThrow(auctionId)).willReturn(mockAuctionVo);
			willThrow(new AuctionException(AuctionErrorCode.ALREADY_CLOSED_AUCTION)).given(mockAuctionVo)
				.validAuctionNotClosed();

			// when & then
			assertThatThrownBy(() -> bidRedisService.createBid(auctionId, mockUser, bidPrice))
				.isInstanceOf(AuctionException.class)
				.hasFieldOrPropertyWithValue("errorCode", AuctionErrorCode.ALREADY_CLOSED_AUCTION);

			then(userDomainService).should().findActiveUserById(userId);
			then(auctionRedisService).should().getAuctionVoElseThrow(auctionId);
			then(mockAuctionVo).should().validAuctionNotClosed();
			then(mockAuctionVo).should(never()).validWonAuction();
		}

		@DisplayName("입찰 생성 실패 - 낙찰된 경매")
		@Test
		void fail_createBid_wonAuction() {
			// given
			Long auctionId = 1L;
			Long userId = 100L;
			Long bidPrice = 1000L;
			User mockUser = mock(User.class);
			given(mockUser.getId()).willReturn(userId);
			AuctionVo mockAuctionVo = mock(AuctionVo.class);

			given(userDomainService.findActiveUserById(userId)).willReturn(mockUser);
			given(auctionRedisService.getAuctionVoElseThrow(auctionId)).willReturn(mockAuctionVo);
			willDoNothing().given(mockAuctionVo).validAuctionNotClosed();
			willThrow(new AuctionException(AuctionErrorCode.ALREADY_WON_AUCTION)).given(mockAuctionVo)
				.validWonAuction();

			// when & then
			assertThatThrownBy(() -> bidRedisService.createBid(auctionId, mockUser, bidPrice))
				.isInstanceOf(AuctionException.class)
				.hasFieldOrPropertyWithValue("errorCode", AuctionErrorCode.ALREADY_WON_AUCTION);

			then(userDomainService).should().findActiveUserById(userId);
			then(auctionRedisService).should().getAuctionVoElseThrow(auctionId);
			then(mockAuctionVo).should().validAuctionNotClosed();
			then(mockAuctionVo).should().validWonAuction();
			then(mockAuctionVo).should(never()).validAuctionOwnerNotBid(anyLong());
		}

		@DisplayName("입찰 생성 실패 - 경매 생성자는 입찰 불가")
		@Test
		void fail_createBid_BidOwnerIsAuctionOwner() {
			// given
			Long auctionId = 1L;
			Long userId = 100L;
			Long bidPrice = 1000L;
			User mockUser = mock(User.class);
			given(mockUser.getId()).willReturn(userId);
			AuctionVo mockAuctionVo = mock(AuctionVo.class);

			given(userDomainService.findActiveUserById(userId)).willReturn(mockUser);
			given(auctionRedisService.getAuctionVoElseThrow(auctionId)).willReturn(mockAuctionVo);
			willDoNothing().given(mockAuctionVo).validAuctionNotClosed();
			willDoNothing().given(mockAuctionVo).validWonAuction();
			willThrow(new BidException(BidErrorCode.CANNOT_BID_OWN_AUCTION)).given(mockAuctionVo)
				.validAuctionOwnerNotBid(userId);

			// when & then
			assertThatThrownBy(() -> bidRedisService.createBid(auctionId, mockUser, bidPrice))
				.isInstanceOf(BidException.class)
				.hasFieldOrPropertyWithValue("errorCode", BidErrorCode.CANNOT_BID_OWN_AUCTION);

			then(userDomainService).should().findActiveUserById(userId);
			then(auctionRedisService).should().getAuctionVoElseThrow(auctionId);
			then(mockAuctionVo).should().validAuctionNotClosed();
			then(mockAuctionVo).should().validWonAuction();
			then(mockAuctionVo).should().validAuctionOwnerNotBid(userId);
			then(mockAuctionVo).should(never()).validMinimumBidPrice(anyLong());
		}

		@DisplayName("입찰 생성 실패 - 최초 입찰 시 시작가보다 낮은 금액")
		@Test
		void fail_createBid_BidPriceBelowStartPrice() {
			// given
			Long auctionId = 1L;
			Long userId = 100L;
			Long bidPrice = 100L;
			User mockUser = mock(User.class);
			given(mockUser.getId()).willReturn(userId);
			AuctionVo mockAuctionVo = mock(AuctionVo.class);

			given(userDomainService.findActiveUserById(userId)).willReturn(mockUser);
			given(auctionRedisService.getAuctionVoElseThrow(auctionId)).willReturn(mockAuctionVo);
			willDoNothing().given(mockAuctionVo).validAuctionNotClosed();
			willDoNothing().given(mockAuctionVo).validWonAuction();
			willDoNothing().given(mockAuctionVo).validAuctionOwnerNotBid(userId);
			willThrow(new BidException(BidErrorCode.BID_PRICE_TOO_LOW_START_PRICE)).given(mockAuctionVo)
				.validMinimumBidPrice(bidPrice);

			// when & then
			assertThatThrownBy(() -> bidRedisService.createBid(auctionId, mockUser, bidPrice))
				.isInstanceOf(BidException.class)
				.hasFieldOrPropertyWithValue("errorCode", BidErrorCode.BID_PRICE_TOO_LOW_START_PRICE);

			then(userDomainService).should().findActiveUserById(userId);
			then(auctionRedisService).should().getAuctionVoElseThrow(auctionId);
			then(mockAuctionVo).should().validAuctionNotClosed();
			then(mockAuctionVo).should().validWonAuction();
			then(mockAuctionVo).should().validAuctionOwnerNotBid(userId);
			then(mockAuctionVo).should().validMinimumBidPrice(bidPrice);
		}

		@DisplayName("입찰 생성 실패 - 현재 경매 입찰가보다 낮은 금액")
		@Test
		void fail_createBid_BidPriceBelowCurrentPrice() {
			// given
			Long auctionId = 1L;
			Long userId = 100L;
			Long bidPrice = 1000L;
			User mockUser = mock(User.class);
			given(mockUser.getId()).willReturn(userId);
			AuctionVo mockAuctionVo = mock(AuctionVo.class);

			given(userDomainService.findActiveUserById(userId)).willReturn(mockUser);
			given(auctionRedisService.getAuctionVoElseThrow(auctionId)).willReturn(mockAuctionVo);
			willDoNothing().given(mockAuctionVo).validAuctionNotClosed();
			willDoNothing().given(mockAuctionVo).validWonAuction();
			willDoNothing().given(mockAuctionVo).validAuctionOwnerNotBid(userId);
			willThrow(new BidException(BidErrorCode.BID_PRICE_TOO_LOW_CURRENT_PRICE)).given(mockAuctionVo)
				.validMinimumBidPrice(bidPrice);

			// when & then
			assertThatThrownBy(() -> bidRedisService.createBid(auctionId, mockUser, bidPrice))
				.isInstanceOf(BidException.class)
				.hasFieldOrPropertyWithValue("errorCode", BidErrorCode.BID_PRICE_TOO_LOW_CURRENT_PRICE);

			then(userDomainService).should().findActiveUserById(userId);
			then(auctionRedisService).should().getAuctionVoElseThrow(auctionId);
			then(mockAuctionVo).should().validAuctionNotClosed();
			then(mockAuctionVo).should().validWonAuction();
			then(mockAuctionVo).should().validAuctionOwnerNotBid(userId);
			then(mockAuctionVo).should().validMinimumBidPrice(bidPrice);
		}
	}

	@Nested
	@DisplayName("입찰 취소 테스트")
	class CancelBidTest {

		@DisplayName("입찰 취소 성공")
		@Test
		void success_cancelBid() {
			// given
			Long auctionId = 1L;
			Long userId = 100L;
			Long bidPrice = 1000L;
			User mockUser = mock(User.class);
			given(mockUser.getId()).willReturn(userId);
			AuctionVo mockAuctionVo = mock(AuctionVo.class);
			BidVo bidVo = mock(BidVo.class);
			given(bidVo.getBidUserId()).willReturn(userId);
			given(bidVo.getBidPrice()).willReturn(bidPrice);
			given(bidVo.getAuctionId()).willReturn(auctionId);
			given(bidVo.getBidUuid()).willReturn("test-uuid");
			AtomicReference<String> bidStatus = new AtomicReference<>(BidStatus.BID.name());
			given(bidVo.getBidStatus()).willAnswer(invocation -> bidStatus.get());
			willAnswer(invocation -> {
				bidStatus.set(BidStatus.CANCEL.name());
				return null;
			}).given(bidVo).cancelBid();
			Set<Object> bidSet = new HashSet<>();
			bidSet.add(bidVo);

			given(userDomainService.findActiveUserById(userId)).willReturn(mockUser);
			given(auctionRedisService.getAuctionVoElseThrow(auctionId)).willReturn(mockAuctionVo);
			given(redisTemplate.opsForZSet()).willReturn(zSetOperations);
			given(zSetOperations.rangeByScore(anyString(), eq((double)bidPrice), eq((double)bidPrice))).willReturn(
				bidSet);
			given(objectMapper.convertValue(any(Object.class), eq(BidVo.class))).willReturn(bidVo);
			given(zSetOperations.reverseRange(anyString(), anyLong(), anyLong())).willReturn(Collections.emptySet());

			// when
			DeleteBidResponse response = bidRedisService.cancelBid(mockUser, auctionId, bidPrice);

			// then
			assertThat(response.auctionId()).isEqualTo(auctionId);
			assertThat(response.bidPrice()).isEqualTo(bidPrice);
			assertThat(response.bidStatus()).isEqualTo(BidStatus.CANCEL.name());

			then(userDomainService).should().findActiveUserById(userId);
			then(auctionRedisService).should().getAuctionVoElseThrow(auctionId);
			then(mockAuctionVo).should().validAuctionNotClosed();
			then(mockAuctionVo).should().validWonAuction();
			then(mockAuctionVo).should().validBidCancelBefore30Minutes();
			then(zSetOperations).should()
				.rangeByScore(eq("bid:auction:" + auctionId), eq((double)bidPrice), eq((double)bidPrice));
			then(bidVo).should().validNotBidOwner(userId);
			then(bidVo).should().validBidStatusIsCancel();
			then(bidVo).should().validBidStatusIsWon();
			then(bidVo).should().validAuctionMismatch(auctionId);
			then(zSetOperations).should().remove(eq("bid:auction:" + auctionId), eq(bidVo));
			then(bidVo).should().cancelBid();
			then(zSetOperations).should().add(eq("bid:auction:" + auctionId), eq(bidVo), eq((double)bidPrice));
			then(auctionRedisService).should().updateAuctionCurrentPrice(auctionId, 0L);
			then(mockUser).should().plusPoint(bidPrice);
			then(userCacheService).should().deleteUserById(userId);
			then(redisMessagePublisher).should()
				.publishAuctionUpdate(eq(auctionId), eq("bid"), any(DeleteBidResponse.class));
			then(bidMetrics).should().countBidCancel();
		}

		@DisplayName("입찰 취소 실패 - 이미 종료된 경매")
		@Test
		void fail_cancelBid_auctionClosed() {
			// given
			Long auctionId = 1L;
			Long userId = 100L;
			Long bidPrice = 1000L;
			User mockUser = mock(User.class);
			given(mockUser.getId()).willReturn(userId);
			AuctionVo mockAuctionVo = mock(AuctionVo.class);

			given(userDomainService.findActiveUserById(userId)).willReturn(mockUser);
			given(auctionRedisService.getAuctionVoElseThrow(auctionId)).willReturn(mockAuctionVo);
			willThrow(new AuctionException(AuctionErrorCode.ALREADY_CLOSED_AUCTION)).given(mockAuctionVo)
				.validAuctionNotClosed();

			// when & then
			assertThatThrownBy(() -> bidRedisService.cancelBid(mockUser, auctionId, bidPrice))
				.isInstanceOf(AuctionException.class)
				.hasFieldOrPropertyWithValue("errorCode", AuctionErrorCode.ALREADY_CLOSED_AUCTION);

			then(userDomainService).should().findActiveUserById(userId);
			then(auctionRedisService).should().getAuctionVoElseThrow(auctionId);
			then(mockAuctionVo).should().validAuctionNotClosed();
			then(mockAuctionVo).should(never()).validWonAuction();
		}

		@DisplayName("입찰 취소 실패 - 낙찰된 경매")
		@Test
		void fail_cancelBid_wonAuction() {
			// given
			Long auctionId = 1L;
			Long userId = 100L;
			Long bidPrice = 1000L;
			User mockUser = mock(User.class);
			given(mockUser.getId()).willReturn(userId);
			AuctionVo mockAuctionVo = mock(AuctionVo.class);

			given(userDomainService.findActiveUserById(userId)).willReturn(mockUser);
			given(auctionRedisService.getAuctionVoElseThrow(auctionId)).willReturn(mockAuctionVo);
			willDoNothing().given(mockAuctionVo).validAuctionNotClosed();
			willThrow(new AuctionException(AuctionErrorCode.ALREADY_WON_AUCTION)).given(mockAuctionVo)
				.validWonAuction();

			// when & then
			assertThatThrownBy(() -> bidRedisService.cancelBid(mockUser, auctionId, bidPrice))
				.isInstanceOf(AuctionException.class)
				.hasFieldOrPropertyWithValue("errorCode", AuctionErrorCode.ALREADY_WON_AUCTION);

			then(userDomainService).should().findActiveUserById(userId);
			then(auctionRedisService).should().getAuctionVoElseThrow(auctionId);
			then(mockAuctionVo).should().validAuctionNotClosed();
			then(mockAuctionVo).should().validWonAuction();
			then(mockAuctionVo).should(never()).validBidCancelBefore30Minutes();
		}

		@DisplayName("입찰 취소 실패 - 경매 종료 30분 전")
		@Test
		void fail_cancelBid_before30Minutes() {
			// given
			Long auctionId = 1L;
			Long userId = 100L;
			Long bidPrice = 1000L;
			User mockUser = mock(User.class);
			given(mockUser.getId()).willReturn(userId);
			AuctionVo mockAuctionVo = mock(AuctionVo.class);

			given(userDomainService.findActiveUserById(userId)).willReturn(mockUser);
			given(auctionRedisService.getAuctionVoElseThrow(auctionId)).willReturn(mockAuctionVo);
			willDoNothing().given(mockAuctionVo).validAuctionNotClosed();
			willDoNothing().given(mockAuctionVo).validWonAuction();
			willThrow(new BidException(BidErrorCode.BID_CANCEL_TIME_LIMIT_EXCEEDED)).given(mockAuctionVo)
				.validBidCancelBefore30Minutes();

			// when & then
			assertThatThrownBy(() -> bidRedisService.cancelBid(mockUser, auctionId, bidPrice))
				.isInstanceOf(BidException.class)
				.hasFieldOrPropertyWithValue("errorCode", BidErrorCode.BID_CANCEL_TIME_LIMIT_EXCEEDED);

			then(userDomainService).should().findActiveUserById(userId);
			then(auctionRedisService).should().getAuctionVoElseThrow(auctionId);
			then(mockAuctionVo).should().validAuctionNotClosed();
			then(mockAuctionVo).should().validWonAuction();
			then(mockAuctionVo).should().validBidCancelBefore30Minutes();
		}
	}

	@Nested
	@DisplayName("특정 경매의 입찰 내역 조회 테스트")
	class FindBidsByAuctionIdTest {

		@DisplayName("조회 성공")
		@Test
		void success_findBidsByAuctionId() {
			// given
			Long auctionId = 1L;
			int page = 0;
			int size = 10;
			Long totalBids = 20L;
			LocalDateTime now = LocalDateTime.now();

			Set<Object> mockBidSet = new HashSet<>();
			for (int i = 0; i < size; i++) {
				BidVo bidVo = mock(BidVo.class);
				given(bidVo.getAuctionId()).willReturn(auctionId);
				given(bidVo.getBidUserId()).willReturn((long)(i + 1));
				given(bidVo.getBidUserNickname()).willReturn("testUser" + (i + 1));
				given(bidVo.getBidPrice()).willReturn((long)(1000 + i));
				given(bidVo.getBidStatus()).willReturn(BidStatus.BID.name());
				given(bidVo.getBidCreatedAt()).willReturn(now.minusMinutes(i));
				mockBidSet.add(bidVo);
			}

			given(redisTemplate.opsForZSet()).willReturn(zSetOperations);
			given(zSetOperations.zCard(anyString())).willReturn(totalBids);
			given(zSetOperations.reverseRange(anyString(), anyLong(), anyLong())).willReturn(mockBidSet);
			given(objectMapper.convertValue(any(Object.class), eq(BidVo.class))).willAnswer(
				invocation -> invocation.getArgument(0));

			// when
			Page<FindBidResponse> result = bidRedisService.findBidsByAuctionId(auctionId, page, size);

			// then
			assertThat(result.getContent()).hasSize(size);
			assertThat(result.getTotalElements()).isEqualTo(totalBids);
			assertThat(result.getNumber()).isEqualTo(page);
			assertThat(result.getTotalPages()).isEqualTo((int)Math.ceil((double)totalBids / size));
			assertThat(result.getSize()).isEqualTo(size);
			assertThat(result.hasContent()).isTrue();

			assertThat(result.getContent())
				.allSatisfy(response -> {
					assertThat(response.auctionId()).isEqualTo(auctionId);
					assertThat(response.bidPrice()).isGreaterThan(0L);
					assertThat(response.bidStatus()).isNotBlank();
					assertThat(response.bidTime()).isNotNull();
					assertThat(response.bidUserNickname()).isNotBlank();
					assertThat(response.bidUserId()).isGreaterThan(0L);
				});

			then(zSetOperations).should().zCard(eq("bid:auction:" + auctionId));
			then(zSetOperations).should()
				.reverseRange(eq("bid:auction:" + auctionId), eq((long)page * size), eq((long)page * size + size - 1));
			then(objectMapper).should(atLeastOnce()).convertValue(any(Object.class), eq(BidVo.class));
		}

		@DisplayName("조회 성공 - 데이터 없음")
		@Test
		void success_findBidsByAuctionId_empty() {
			// given
			Long auctionId = 1L;
			int page = 0;
			int size = 10;
			given(redisTemplate.opsForZSet()).willReturn(zSetOperations);
			given(zSetOperations.zCard(anyString())).willReturn(0L);

			// when
			Page<FindBidResponse> result = bidRedisService.findBidsByAuctionId(auctionId, page, size);

			// then
			assertThat(result).isNotNull().isEmpty();
			assertThat(result.getNumber()).isEqualTo(page);
			assertThat(result.getSize()).isEqualTo(size);

			then(zSetOperations).should().zCard(eq("bid:auction:" + auctionId));
			then(zSetOperations).should(never()).reverseRange(anyString(), anyLong(), anyLong());
		}
	}

	@Nested
	@DisplayName("내 입찰 내역 전체 조회 테스트")
	class FindMyBidsTest {

		@DisplayName("조회 성공")
		@Test
		void success_findMyBidVoList() {
			// given
			Long userId = 100L;
			List<Long> auctionIds = List.of(1L, 2L);
			LocalDateTime now = LocalDateTime.now();

			BidVo bid1 = mock(BidVo.class);
			given(bid1.getBidUserId()).willReturn(userId);
			given(bid1.getBidCreatedAt()).willReturn(now.minusMinutes(10));

			BidVo bid2 = mock(BidVo.class);
			given(bid2.getBidUserId()).willReturn(userId);
			given(bid2.getBidCreatedAt()).willReturn(now.minusMinutes(5));

			BidVo bid3 = mock(BidVo.class);
			given(bid3.getBidUserId()).willReturn(userId);
			given(bid3.getBidCreatedAt()).willReturn(now.minusMinutes(15));

			Set<Object> auction1Bids = new HashSet<>();
			auction1Bids.add(bid1);
			auction1Bids.add(bid2);
			Set<Object> auction2Bids = new HashSet<>();
			auction2Bids.add(bid3);

			given(redisTemplate.opsForZSet()).willReturn(zSetOperations);
			given(zSetOperations.range(eq("bid:auction:1"), anyLong(), anyLong())).willReturn(auction1Bids);
			given(zSetOperations.range(eq("bid:auction:2"), anyLong(), anyLong())).willReturn(auction2Bids);
			given(objectMapper.convertValue(any(Object.class), eq(BidVo.class))).willAnswer(
				invocation -> invocation.getArgument(0));

			// when
			List<BidVo> result = bidRedisService.findMyBidVoList(userId, auctionIds);

			// then
			assertThat(result)
				.hasSize(3)
				.containsExactlyInAnyOrder(bid1, bid2, bid3)
				.allSatisfy(bidVo -> {
					assertThat(bidVo.getBidUserId()).isEqualTo(userId);
					assertThat(bidVo.getBidCreatedAt()).isNotNull();
				});

			assertThat(result.get(0).getBidCreatedAt()).isAfterOrEqualTo(result.get(1).getBidCreatedAt());
			assertThat(result.get(1).getBidCreatedAt()).isAfterOrEqualTo(result.get(2).getBidCreatedAt());

			then(zSetOperations).should().range(eq("bid:auction:1"), eq(0L), eq(-1L));
			then(zSetOperations).should().range(eq("bid:auction:2"), eq(0L), eq(-1L));
			then(objectMapper).should(times(3)).convertValue(any(Object.class), eq(BidVo.class));
		}

		@DisplayName("조회 성공 - 데이터 없음")
		@Test
		void success_findMyBidVoList_empty() {
			// given
			Long userId = 100L;
			List<Long> auctionIds = List.of(1L, 2L);
			given(redisTemplate.opsForZSet()).willReturn(zSetOperations);
			given(zSetOperations.range(anyString(), anyLong(), anyLong())).willReturn(Collections.emptySet());

			// when
			List<BidVo> result = bidRedisService.findMyBidVoList(userId, auctionIds);

			// then
			assertThat(result).isNotNull().isEmpty();
			then(zSetOperations).should(times(auctionIds.size())).range(anyString(), anyLong(), anyLong());
			then(objectMapper).should(never()).convertValue(any(Object.class), any(Class.class));
		}
	}

	@Nested
	@DisplayName("Redis에 저장된 낙찰 예정인 입찰 정보 조회 테스트")
	class FindWonBidVo {

		@DisplayName("조회 성공")
		@Test
		void success_findWonBidVo() {
			// given
			Long auctionId = 1L;
			Long bidPrice = 1000L;
			BidVo expectedBidVo = mock(BidVo.class);
			Set<Object> bidSet = new HashSet<>();
			bidSet.add(expectedBidVo);

			given(redisTemplate.opsForZSet()).willReturn(zSetOperations);
			given(zSetOperations.rangeByScore(anyString(), eq((double)bidPrice), eq((double)bidPrice))).willReturn(
				bidSet);
			given(objectMapper.convertValue(any(Object.class), eq(BidVo.class))).willReturn(expectedBidVo);

			// when
			BidVo result = bidRedisService.findWonBidVo(auctionId, bidPrice);

			// then
			assertThat(result).isEqualTo(expectedBidVo);
			then(zSetOperations).should()
				.rangeByScore(eq("bid:auction:" + auctionId), eq((double)bidPrice), eq((double)bidPrice));
			then(objectMapper).should().convertValue(any(Object.class), eq(BidVo.class));
		}

		@DisplayName("조회 성공 - 데이터 없음")
		@Test
		void fail_findWonBidVo_empty() {
			// given
			Long auctionId = 1L;
			Long bidPrice = 1000L;
			given(redisTemplate.opsForZSet()).willReturn(zSetOperations);
			given(zSetOperations.rangeByScore(anyString(), eq((double)bidPrice), eq((double)bidPrice))).willReturn(
				Collections.emptySet());

			// when
			BidVo result = bidRedisService.findWonBidVo(auctionId, bidPrice);

			// then
			assertThat(result).isNull();
			then(zSetOperations).should()
				.rangeByScore(eq("bid:auction:" + auctionId), eq((double)bidPrice), eq((double)bidPrice));
			then(objectMapper).should(never()).convertValue(any(Object.class), any(Class.class));
		}
	}
}
