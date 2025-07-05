package nbc.chillguys.nebulazone.application.bid.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayName("입찰 서비스 단위 테스트")
@ExtendWith(MockitoExtension.class)
class BidServiceTest {

	@Nested
	@DisplayName("특정 경매의 입찰 내역 조회 테스트")
	class FindBidsByAuctionIdTest {

		@DisplayName("조회 성공 - 진행중 경매(레디스)")
		@Test
		void success_findBidsByAuctionId_activeAuction() {
			// given

			// when

			// then
		}

		@DisplayName("조회 성공 - 종료된 경매(RDB)")
		@Test
		void success_findBidsByAuctionId_mySQL() {
			// given
			// 레디스 조회가 empty()

			// when

			// then
		}
	}

	@Nested
	@DisplayName("내 입찰 내역 전체 조회 테스트")
	class FindMyBidsTest {

		@DisplayName("조회 성공")
		@Test
		void success_findMyBids() {
			// given

			// when

			// then
		}

		@DisplayName("조회 성공 - 진행 중인 경매에만 입찰")
		@Test
		void success_findMyBids_onlyRedis() {
			// given

			// when

			// then
		}

		@DisplayName("조회 성공 - 종료된 경매에만 입찰")
		@Test
		void success_findMyBids_onlyMySql() {
			// given

			// when

			// then
		}

	}

}
