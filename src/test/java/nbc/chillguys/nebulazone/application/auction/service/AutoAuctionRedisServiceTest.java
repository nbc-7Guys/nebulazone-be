package nbc.chillguys.nebulazone.application.auction.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayName("자동 경매 서비스 단위 테스트")
@ExtendWith(MockitoExtension.class)
class AutoAuctionRedisServiceTest {

	@Nested
	@DisplayName("경매 자동낙찰 테스트")
	class AutoEndAuction {

		@DisplayName("자동낙찰 성공")
		@Test
		void success_processAuctionEnding() {
			// given

			// when

			// then
		}
	}
}
