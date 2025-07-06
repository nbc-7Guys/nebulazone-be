package nbc.chillguys.nebulazone.application.bid.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import nbc.chillguys.nebulazone.application.auction.service.AuctionRedisService;
import nbc.chillguys.nebulazone.application.bid.dto.response.FindBidResponse;
import nbc.chillguys.nebulazone.application.bid.dto.response.FindMyBidsResponse;
import nbc.chillguys.nebulazone.common.response.CommonPageResponse;
import nbc.chillguys.nebulazone.domain.auction.service.AuctionDomainService;
import nbc.chillguys.nebulazone.domain.bid.dto.FindBidsByAuctionInfo;
import nbc.chillguys.nebulazone.domain.bid.entity.BidStatus;
import nbc.chillguys.nebulazone.domain.bid.service.BidDomainService;
import nbc.chillguys.nebulazone.domain.user.entity.User;

@DisplayName("입찰 서비스 단위 테스트")
@ExtendWith(MockitoExtension.class)
class BidServiceTest {

	@Mock
	private BidDomainService bidDomainService;
	@Mock
	private AuctionDomainService auctionDomainService;
	@Mock
	private AuctionRedisService auctionRedisService;
	@Mock
	private BidRedisService bidRedisService;

	@InjectMocks
	private BidService bidService;

	@Nested
	@DisplayName("특정 경매의 입찰 내역 조회 테스트")
	class FindBidsByAuctionIdTest {

		@DisplayName("조회 성공 - 진행중 경매(레디스)")
		@Test
		void success_findBidsByAuctionId_activeAuction() {
			// given
			Long auctionId = 1L;
			int page = 0;
			int size = 10;
			LocalDateTime now = LocalDateTime.now();

			FindBidResponse mockResponse = new FindBidResponse(
				100L, "testUser", "BID", 1000L, now, auctionId
			);

			Page<FindBidResponse> mockRedisPage = new PageImpl<>(
				List.of(mockResponse), PageRequest.of(page, size), 1
			);

			when(bidRedisService.findBidsByAuctionId(auctionId, page, size))
				.thenReturn(mockRedisPage);

			// when
			CommonPageResponse<FindBidResponse> result = bidService.findBidsByAuctionId(auctionId, page, size);

			// then
			assertThat(result).isNotNull();
			assertThat(result.content()).hasSize(1);
			assertThat(result.totalElements()).isEqualTo(1L);
			assertThat(result.totalPages()).isEqualTo(1);
			assertThat(result.page()).isEqualTo(page + 1);
			assertThat(result.size()).isEqualTo(size);

			FindBidResponse responseContent = result.content().get(0);
			assertThat(responseContent.bidUserId()).isEqualTo(100L);
			assertThat(responseContent.bidUserNickname()).isEqualTo("testUser");
			assertThat(responseContent.bidStatus()).isEqualTo("BID");
			assertThat(responseContent.bidPrice()).isEqualTo(1000L);
			assertThat(responseContent.auctionId()).isEqualTo(auctionId);

			verify(bidRedisService).findBidsByAuctionId(auctionId, page, size);
			verify(auctionDomainService, never()).existsAuctionByIdElseThrow(anyLong());
			verify(bidDomainService, never()).findBidsByAuctionId(anyLong(), anyInt(), anyInt());
		}

		@DisplayName("조회 성공 - 종료된 경매(RDB)")
		@Test
		void success_findBidsByAuctionId_mySql() {
			// given
			Long auctionId = 1L;
			int page = 0;
			int size = 10;
			LocalDateTime now = LocalDateTime.now();

			// Redis 조회 결과가 empty
			Page<FindBidResponse> emptyRedisPage = new PageImpl<>(Collections.emptyList());
			when(bidRedisService.findBidsByAuctionId(auctionId, page, size))
				.thenReturn(emptyRedisPage);

			// RDB 조회 결과
			FindBidsByAuctionInfo mockInfo = new FindBidsByAuctionInfo(
				100L, 1000L, now, BidStatus.BID, "testUser", auctionId
			);
			Page<FindBidsByAuctionInfo> mockDbPage = new PageImpl<>(
				List.of(mockInfo), PageRequest.of(page, size), 1
			);

			when(bidDomainService.findBidsByAuctionId(auctionId, page, size))
				.thenReturn(mockDbPage);

			// when
			CommonPageResponse<FindBidResponse> result = bidService.findBidsByAuctionId(auctionId, page, size);

			// then
			assertThat(result).isNotNull();
			assertThat(result.content()).hasSize(1);
			assertThat(result.totalElements()).isEqualTo(1L);
			assertThat(result.totalPages()).isEqualTo(1);
			assertThat(result.page()).isEqualTo(page + 1);
			assertThat(result.size()).isEqualTo(size);

			FindBidResponse responseContent = result.content().get(0);
			assertThat(responseContent.bidUserId()).isEqualTo(100L);
			assertThat(responseContent.bidUserNickname()).isEqualTo("testUser");
			assertThat(responseContent.bidStatus()).isEqualTo("BID");
			assertThat(responseContent.bidPrice()).isEqualTo(1000L);
			assertThat(responseContent.auctionId()).isEqualTo(auctionId);

			verify(bidRedisService).findBidsByAuctionId(auctionId, page, size);
			verify(auctionDomainService).existsAuctionByIdElseThrow(auctionId);
			verify(bidDomainService).findBidsByAuctionId(auctionId, page, size);
		}
	}

	@Nested
	@DisplayName("내 입찰 내역 전체 조회 테스트")
	class FindMyBidsTest {

		@DisplayName("조회 성공")
		@Test
		void success_findMyBids() {
			// given
			Long userId = 100L;
			int page = 0;
			int size = 10;

			User mockUser = mock(User.class);
			when(mockUser.getId()).thenReturn(userId);

			// 빈 리스트 반환
			when(bidDomainService.findMyBids(userId))
				.thenReturn(Collections.emptyList());
			when(auctionRedisService.findAllAuctionVoIds())
				.thenReturn(Collections.emptyList());
			when(bidRedisService.findMyBidVoList(userId, Collections.emptyList()))
				.thenReturn(Collections.emptyList());

			// when
			CommonPageResponse<FindMyBidsResponse> result = bidService.findMyBids(mockUser, page, size);

			// then
			assertThat(result).isNotNull();
			assertThat(result.content()).isEmpty();
			assertThat(result.totalElements()).isEqualTo(0L);
			assertThat(result.page()).isEqualTo(page + 1);

			verify(bidDomainService).findMyBids(userId);
			verify(auctionRedisService).findAllAuctionVoIds();
			verify(bidRedisService).findMyBidVoList(userId, Collections.emptyList());
		}

		@DisplayName("조회 성공 - 진행 중인 경매에만 입찰")
		@Test
		void success_findMyBids_onlyRedis() {
			// given
			Long userId = 100L;
			int page = 0;
			int size = 10;

			User mockUser = mock(User.class);
			when(mockUser.getId()).thenReturn(userId);

			// RDB 데이터 없음
			when(bidDomainService.findMyBids(userId))
				.thenReturn(Collections.emptyList());

			// Redis 데이터만 존재
			when(auctionRedisService.findAllAuctionVoIds())
				.thenReturn(Collections.emptyList());
			when(bidRedisService.findMyBidVoList(userId, Collections.emptyList()))
				.thenReturn(Collections.emptyList());

			// when
			CommonPageResponse<FindMyBidsResponse> result = bidService.findMyBids(mockUser, page, size);

			// then
			assertThat(result).isNotNull();
			assertThat(result.content()).isEmpty();
			assertThat(result.totalElements()).isEqualTo(0L);
			assertThat(result.page()).isEqualTo(page + 1);

			verify(bidDomainService).findMyBids(userId);
			verify(auctionRedisService).findAllAuctionVoIds();
			verify(bidRedisService).findMyBidVoList(userId, Collections.emptyList());
		}

		@DisplayName("조회 성공 - 종료된 경매에만 입찰")
		@Test
		void success_findMyBids_onlyMySql() {
			// given
			Long userId = 100L;
			int page = 0;
			int size = 10;

			User mockUser = mock(User.class);
			when(mockUser.getId()).thenReturn(userId);

			// 빈 리스트 반환
			when(bidDomainService.findMyBids(userId))
				.thenReturn(Collections.emptyList());
			when(auctionRedisService.findAllAuctionVoIds())
				.thenReturn(Collections.emptyList());
			when(bidRedisService.findMyBidVoList(userId, Collections.emptyList()))
				.thenReturn(Collections.emptyList());

			// when
			CommonPageResponse<FindMyBidsResponse> result = bidService.findMyBids(mockUser, page, size);

			// then
			assertThat(result).isNotNull();
			assertThat(result.content()).isEmpty();
			assertThat(result.totalElements()).isEqualTo(0L);
			assertThat(result.page()).isEqualTo(page + 1);

			verify(bidDomainService).findMyBids(userId);
			verify(auctionRedisService).findAllAuctionVoIds();
			verify(bidRedisService).findMyBidVoList(userId, Collections.emptyList());
		}

	}

}
