package nbc.chillguys.nebulazone.domain.auction.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import nbc.chillguys.nebulazone.domain.auction.entity.Auction;
import nbc.chillguys.nebulazone.domain.auction.repository.AuctionRepository;

@DisplayName("자동 경매 도메인 서비스 단위 테스트")
@ExtendWith(MockitoExtension.class)
class AutoAuctionDomainServiceTest {

	@Mock
	private AuctionRepository auctionRepository;

	@InjectMocks
	private AutoAuctionDomainService autoAuctionDomainService;

	@Nested
	@DisplayName("경매 자동 종료 테스트")
	class AutoEndAuctionTest {

		@DisplayName("경매 자동 종료 성공 - 낙찰")
		@Test
		void success_autoEndAuction_won() {
			// given
			Long auctionId = 1L;
			Long bidPrice = 1000L;
			Auction mockAuction = mock(Auction.class);

			given(auctionRepository.findAuctionWithProductAndSeller(auctionId)).willReturn(Optional.of(mockAuction));
			given(mockAuction.isNotWonAndNotDeleted()).willReturn(true);

			// when
			Auction result = autoAuctionDomainService.autoEndAuction(auctionId, bidPrice);

			// then
			assertThat(result).isEqualTo(mockAuction);
			then(mockAuction).should().wonAuction();
			then(mockAuction).should().updateBidPrice(bidPrice);
			then(mockAuction).should().updateEndTime();
		}

		@DisplayName("경매 자동 종료 성공 - 유찰")
		@Test
		void success_autoEndAuction_unSold() {
			// given
			Long auctionId = 1L;
			Long bidPrice = 0L; // 입찰자가 없어 입찰가가 0원인 경우
			Auction mockAuction = mock(Auction.class);

			given(auctionRepository.findAuctionWithProductAndSeller(auctionId)).willReturn(Optional.of(mockAuction));
			given(mockAuction.isNotWonAndNotDeleted()).willReturn(true);

			// when
			Auction result = autoAuctionDomainService.autoEndAuction(auctionId, bidPrice);

			// then
			assertThat(result).isEqualTo(mockAuction);
			then(mockAuction).should(never()).wonAuction();
			then(mockAuction).should(never()).updateBidPrice(anyLong());
			then(mockAuction).should(never()).updateEndTime();
		}

		@DisplayName("경매 자동 종료 실패 - 낙찰 대상 경매 없음")
		@Test
		void fail_autoEndAuction_empty() {
			// given
			Long auctionId = 1L;
			Long bidPrice = 1000L;

			given(auctionRepository.findAuctionWithProductAndSeller(auctionId)).willReturn(Optional.empty());

			// when
			Auction result = autoAuctionDomainService.autoEndAuction(auctionId, bidPrice);

			// then
			assertThat(result).isNull();
			then(auctionRepository).should().findAuctionWithProductAndSeller(auctionId);
		}
	}
}
