package nbc.chillguys.nebulazone.domain.bid.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.time.LocalDateTime;
import java.util.Arrays;
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
import org.springframework.data.domain.Pageable;

import nbc.chillguys.nebulazone.domain.bid.dto.FindBidsByAuctionInfo;
import nbc.chillguys.nebulazone.domain.bid.dto.FindMyBidsInfo;
import nbc.chillguys.nebulazone.domain.bid.entity.Bid;
import nbc.chillguys.nebulazone.domain.bid.entity.BidStatus;
import nbc.chillguys.nebulazone.domain.bid.repository.BidRepository;

@DisplayName("입찰 도메인 서비스 단위 테스트")
@ExtendWith(MockitoExtension.class)
class BidDomainServiceTest {

	@Mock
	private BidRepository bidRepository;

	@InjectMocks
	private BidDomainService bidDomainService;

	@Nested
	@DisplayName("입찰 배치 저장 테스트")
	class CreateAllBidTest {

		@DisplayName("입찰 배치 저장 성공")
		@Test
		void success_createAllBid() {
			// given
			List<Bid> bids = Arrays.asList(
				Bid.builder().price(1000L).status(BidStatus.BID.name()).build(),
				Bid.builder().price(2000L).status(BidStatus.BID.name()).build()
			);
			given(bidRepository.saveAll(bids)).willReturn(bids);

			// when
			bidDomainService.saveAllBids(bids);

			// then
			verify(bidRepository, times(1)).saveAll(bids);
			assertThat(bids).isNotEmpty();
		}
	}

	@Nested
	@DisplayName("내 입찰 내역 전체 조회 테스트")
	class FindMyBidsTest {

		@DisplayName("전체 조회 성공")
		@Test
		void success_findMyBids() {
			// given
			Long userId = 1L;
			List<FindMyBidsInfo> expectedBids = Arrays.asList(
				new FindMyBidsInfo(100L, "testUser1", BidStatus.BID, 1000L, LocalDateTime.now(), 1L, 1L, "product1"),
				new FindMyBidsInfo(100L, "testUser1", BidStatus.BID, 2000L, LocalDateTime.now(), 2L, 2L, "product2")
			);
			given(bidRepository.findMyBids(userId)).willReturn(expectedBids);

			// when
			List<FindMyBidsInfo> actualBids = bidDomainService.findMyBids(userId);

			// then
			assertThat(actualBids).isEqualTo(expectedBids);
			assertThat(actualBids).hasSize(2);
			assertThat(actualBids.get(0).bidPrice()).isEqualTo(1000L);
			assertThat(actualBids.get(1).auctionId()).isEqualTo(2L);
			verify(bidRepository, times(1)).findMyBids(userId);
		}
	}

	@Nested
	@DisplayName("특정 경매의 낙찰 상태인 입찰 조회 테스트")
	class FindWonBidByAuctionIdTest {

		@DisplayName("조회 성공")
		@Test
		void success_findWonBidByAuctionIdTest() {
			// given
			Long auctionId = 1L;
			Bid expectedBid = Bid.builder().price(1000L).status(BidStatus.WON.name()).build();
			given(bidRepository.findHighestPriceBidByAuctionWithUser(auctionId)).willReturn(expectedBid);

			// when
			Bid actualBid = bidDomainService.findWonBidByAuctionId(auctionId);

			// then
			assertThat(actualBid).isEqualTo(expectedBid);
			assertThat(actualBid.getPrice()).isEqualTo(1000L);
			assertThat(actualBid.getStatus()).isEqualTo(BidStatus.WON);
			verify(bidRepository, times(1)).findHighestPriceBidByAuctionWithUser(auctionId);
		}
	}

	@Nested
	@DisplayName("특정 경매의 입찰 내역 전체 조회 테스트")
	class FindBidsByAuctionIdTest {

		@DisplayName("조회 성공 - 페이징")
		@Test
		void success_findBidsByAuctionId() {
			// given
			Long auctionId = 1L;
			int page = 0;
			int size = 10;
			Pageable pageable = PageRequest.of(page, size);
			List<FindBidsByAuctionInfo> expectedBids = Arrays.asList(
				new FindBidsByAuctionInfo(1L, 1000L, LocalDateTime.now(), BidStatus.BID, "user1", 1L),
				new FindBidsByAuctionInfo(2L, 2000L, LocalDateTime.now(), BidStatus.BID, "user2", 1L)
			);
			Page<FindBidsByAuctionInfo> expectedPage = new PageImpl<>(expectedBids, pageable, expectedBids.size());
			given(bidRepository.findBidsWithUserByAuctionId(auctionId, page, size)).willReturn(expectedPage);

			// when
			Page<FindBidsByAuctionInfo> actualPage = bidDomainService.findBidsByAuctionId(auctionId, page, size);

			// then
			assertThat(actualPage).isEqualTo(expectedPage);
			assertThat(actualPage.getContent()).hasSize(2);
			assertThat(actualPage.getContent().get(0).bidPrice()).isEqualTo(1000L);
			assertThat(actualPage.getTotalElements()).isEqualTo(2L);
			verify(bidRepository, times(1)).findBidsWithUserByAuctionId(auctionId, page, size);
		}
	}

}
