package nbc.chillguys.nebulazone.application.bid.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;

import nbc.chillguys.nebulazone.config.TestMockConfig;
import nbc.chillguys.nebulazone.config.TestSecurityConfig;
import nbc.chillguys.nebulazone.infra.security.filter.JwtAuthenticationFilter;

@DisplayName("입찰 컨트롤러 단위 테스트")
@Import({TestSecurityConfig.class, TestMockConfig.class})
@WebMvcTest(
	controllers = BidController.class,
	excludeFilters = {
		@ComponentScan.Filter(
			type = FilterType.ASSIGNABLE_TYPE,
			classes = {
				JwtAuthenticationFilter.class
			})
	})
class BidControllerTest {

	@Nested
	@DisplayName("입찰 생성 테스트")
	class CreateBidTest {

		@DisplayName("입찰 생성 성공")
		@Test
		void success_createBid() {
			// given

			// when

			// then
		}

	}

	@Nested
	@DisplayName("특정 경매의 입찰 전체 조회 테스트")
	class FindBidsByAuctionIdTest {

		@DisplayName("특정 경매의 입찰 전체 조회 성공")
		@Test
		void success_findBidsByAuctionId() {
			// given

			// when

			// then
		}

	}

	@Nested
	@DisplayName("내 입찰 전체 조회 테스트")
	class FindMyBidsTest {
		@DisplayName("내 입찰 전체 조회 성공")
		@Test
		void success_findMyBids() {
			// given

			// when

			// then
		}
	}

	@Nested
	@DisplayName("입찰 취소 테스트")
	class CancelBid {
		@DisplayName("입찰 취소 성공")
		void success_cancelBid() {

		}
	}
}
