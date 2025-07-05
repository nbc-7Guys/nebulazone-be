package nbc.chillguys.nebulazone.domain.bid.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayName("입찰 도메인 서비스 단위 테스트")
@ExtendWith(MockitoExtension.class)
class BidDomainServiceTest {

	@Nested
	@DisplayName("입찰 배치 저장 테스트")
	class CreateAllBidTest {

		@DisplayName("입찰 배치 저장 성공")
		@Test
		void success_createAllBid() {
			// given

			// when

			// then
		}
	}

	@Nested
	@DisplayName("내 입찰 내역 전체 조회 테스트")
	class FindMyBidsTest {

		@DisplayName("전체 조회 성공")
		@Test
		void success_findMyBids() {
			// given

			// when

			// then
		}
	}

	@Nested
	@DisplayName("특정 경매의 낙찰 상태인 입찰 조회 테스트")
	class FindWonBidByAuctionIdTest {

		@DisplayName("조회 성공")
		@Test
		void success_findWonBidByAuctionIdTest() {
			// given

			// when

			// then
		}
	}

	@Nested
	@DisplayName("특정 경매의 입찰 내역 전체 조회 테스트")
	class FindBidsByAuctionIdTest {

		@DisplayName("조회 성공 - 페이징")
		@Test
		void success_findBidsByAuctionId() {
			// given

			// when

			// then
		}
	}

}
