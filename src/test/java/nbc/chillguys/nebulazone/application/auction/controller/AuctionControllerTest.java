package nbc.chillguys.nebulazone.application.auction.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;

import nbc.chillguys.nebulazone.config.TestSecurityConfig;
import nbc.chillguys.nebulazone.infra.security.filter.JwtAuthenticationFilter;
import nbc.chillguys.nebulazone.support.mock.TestMockConfig;

@DisplayName("경매 컨트롤러 단위 테스트")
@Import({TestSecurityConfig.class, TestMockConfig.class})
@WebMvcTest(
	controllers = AuctionController.class,
	excludeFilters = {
		@ComponentScan.Filter(
			type = FilterType.ASSIGNABLE_TYPE,
			classes = {
				JwtAuthenticationFilter.class
			})
	}
)
class AuctionControllerTest {

	@Nested
	@DisplayName("정렬 기준 경매 리스트 조회 테스트")
	class FindAuctionSortTypeTest {

		@DisplayName("조회 성공 - 마감 임박순 경매, 5개")
		@Test
		void success_findAuctionSortType_closing() {
			// given

			// when

			// then
		}

		@DisplayName("조회 성공 - 인기순(입찰개수) 경매, 5개")
		@Test
		void success_findAuctionSortType_popular() {
			// given

			// when

			// then
		}
	}

	@Nested
	@DisplayName("경매 상세 조회 테스트")
	class FindAuctionDetailTest {

		@DisplayName("경매 상세 조회 성공")
		@Test
		void success_findAuction() {
			// given

			// when

			// then
		}
	}

	@Nested
	@DisplayName("경매 수동 낙찰 테스트")
	class ManualEndAuctionTest {

		@DisplayName("경매 수동 낙찰 성공")
		@Test
		void success_manualEndAuction() {
			// given

			// when

			// then
		}
	}

	@Nested
	@DisplayName("경매 삭제 테스트")
	class DeleteAuctionTest {

		@DisplayName("경매 삭제 성공")
		@Test
		void success_deleteAuction() {
			// given

			// when

			// then
		}
	}
}
