package nbc.chillguys.nebulazone.application.auction.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayName("경매 서비스 단위 테스트")
@ExtendWith(MockitoExtension.class)
class AuctionServiceTest {

	@Nested
	@DisplayName("경매 상세 조회 테스트")
	class FindAuction {

		@DisplayName("경세 상세 조회 성공 - 진행중인 경매(레디스)")
		@Test
		void success_findAuction_redis() {
			// given

			// when

			// then
		}

		@DisplayName("경세 상세 조회 성공 - 진행중인 경매(레디스)")
		@Test
		void success_findAuction_rdb() {
			// given

			// when

			// then
		}
	}
}
