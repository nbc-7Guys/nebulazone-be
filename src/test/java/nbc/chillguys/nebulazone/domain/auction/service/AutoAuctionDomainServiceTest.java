package nbc.chillguys.nebulazone.domain.auction.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayName("자동 경매 도메인 서비스 단위 테스트")
@ExtendWith(MockitoExtension.class)
class AutoAuctionDomainServiceTest {

	@Nested
	@DisplayName("경매 자동 종료 테스트")
	class AutoEndAuctionTest {

		@DisplayName("경매 자동 종료 성공 - 낙찰")
		@Test
		void success_autoEndAuction_won() {
			// given

			// when

			// then
		}

		@DisplayName("경매 자동 종료 성공 - 유찰")
		@Test
		void success_autoEndAuction_unSold() {
			// given

			// when

			// then
		}

		@DisplayName("경매 자동 종료 실패 - 낙찰 대상 경매 없음")
		@Test
		void fail_autoEndAuction_empty() {
			// given

			// when

			// then
			// null
		}
	}

}
