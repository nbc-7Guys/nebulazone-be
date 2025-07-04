package nbc.chillguys.nebulazone.application.auction.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayName("경매 레디스 서비스 단위 테스트")
@ExtendWith(MockitoExtension.class)
class AuctionRedisServiceTest {

	@Nested
	@DisplayName("경매 생성 테스트")
	class CreateAuctionTest {

		@DisplayName("레디스 경매 생성 성공")
		@Test
		void success_createAuction() {
		}
	}

	@Nested
	@DisplayName("경매 수동 낙찰 테스트")
	class ManualEndAuctionTest {

		@DisplayName("수동 낙찰 성공")
		@Test
		void success_manualEndAuction() {

		}

		@DisplayName("수동 낙찰 실패 - 경매 생성자가 아님")
		@Test
		void fail_manualEndAuction_notAuctionOwner() {

		}

		@DisplayName("수동 낙찰 실패 - 낙찰 요청 입찰가와 경매에 등록된 입찰가가 다름")
		@Test
		void fail_manualEndAuction_mismatchBidPrice() {

		}

		@DisplayName("수동 낙찰 실패 - 종료된 경매")
		@Test
		void fail_manualEndAuction_closed() {

		}

		@DisplayName("수동 낙찰 실패 - 낙찰된 경매")
		@Test
		void fail_manualEndAuction_won() {

		}

		@DisplayName("수동 낙찰 실패 - 경매에 등록된 입찰 유저와 낙찰 시 요청된 입찰 유저가 다름")
		@Test
		void fail_manualEndAuction_mismatchBidUser() {

		}
	}

	@Nested
	@DisplayName("경매 삭제 낙찰 테스트")
	class DeleteAuctionTest {

		@DisplayName("경매 삭제 성공")
		@Test
		void success_deleteAuction() {
			// given

			// when

			// then
		}

		@DisplayName("경매 삭제 실패 - 경매 생성자가 아님")
		@Test
		void fail_deleteAuction_notAuctionOwner() {
			// given

			// when

			// then
		}

	}

	@Nested
	@DisplayName("정렬 기반 경매 조회 테스트")
	class FindAuctionSortTypeTest {

		@DisplayName("정렬 기반 경매 조회 성공 - 마감 임박순")
		@Test
		void success_findAuctionsBySortType_closing() {
			// given

			// when

			// then
		}

		@DisplayName("정렬 기반 경매 조회 성공 - 인기순(입찰개수)")
		@Test
		void success_findAuctionsBySortType_popular() {
			// given

			// when

			// then
		}
	}

	@Nested
	@DisplayName("레디스에 저장된 특정 경매 조회 테스트 - api 요청시 사용")
	class GetAuctionVoElseThrowTest {

		@DisplayName("조회 성공")
		@Test
		void success_getAuctionVoElseThrow() {
			// given

			// when

			// then
		}

		@DisplayName("조회 실패 - 레디스에 없음")
		@Test
		void success_getAuctionVoElseThrow_notFound() {
			// given

			// when

			// then
			// 에러 발생

		}
	}

	@Nested
	@DisplayName("레디스에 저장된 특정 경매 조회 테스트 - 자동 로직 동작시 사용")
	class FindRedisAuctionVoTest {
		@DisplayName("조회 성공")
		@Test
		void success_findRedisAuctionVo() {
			// given

			// when

			// then
		}

		@DisplayName("조회 실패 - 레디스에 없음")
		@Test
		void success_findRedisAuctionVo_notFound() {
			// given

			// when

			// then
			// null 반환
		}
	}

	@Nested
	@DisplayName("특정 경매의 입찰 최고가 갱신 테스트")
	class UpdateAuctionCurrentPriceTest {

		@DisplayName("갱신 성공")
		@Test
		void success_updateAuctionCurrentPrice() {
			// given

			// when

			// then

		}
	}

	@Nested
	@DisplayName("레디스 전체 경매의 id를 조회 테스트")
	class FindAllAuctionIdsTest {

		@DisplayName("조회 성공")
		@Test
		void success_findAllAuctionVoIds() {
			// given

			// when

			// then

		}

		@DisplayName("조회 성공 - 0건")
		@Test
		void success_findAllAuctionVoIds_empty() {
			// given

			// when

			// then

		}
	}

	@Nested
	@DisplayName("레디스 경매 데이터의 이미지 url 수정")
	class UpdateAuctionProductImagesTest {

		@DisplayName("조회 성공")
		@Test
		void success_updateAuctionProductImagesTest() {
			// given

			// when

			// then

		}
	}
}
