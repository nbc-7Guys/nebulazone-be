package nbc.chillguys.nebulazone.domain.auction.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayName("경매 도메인 서비스 단위 테스트")
@ExtendWith(MockitoExtension.class)
class AuctionDomainServiceTest {

	@Nested
	@DisplayName("경매 생성 테스트")
	class CreateAuctionTest {

		@DisplayName("경매 생성 성공")
		@Test
		void success_createAuction() {
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
		void success_findAuctionDetailInfoByAuctionId() {
			// given

			// when

			// then

		}

		@DisplayName("경매 상세 조회 실패 - 경매 없음")
		@Test
		void fail_findAuctionDetailInfoByAuctionId_notFound() {
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

		@DisplayName("경매 수동 낙찰 실패 - 삭제된 경매")
		@Test
		void fail_manualEndAuction_alreadyDeleted() {
			// given

			// when

			// then
			// ALREADY_DELETED_AUCTION

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

		@DisplayName("경매 삭제 실패 - 경매 없음")
		@Test
		void fail_deleteAuction_notFound() {
			// given

			// when

			// then
		}
	}

	@Nested
	@DisplayName("상품 아이디로 경매 단건 조회 테스트")
	class FindAuctionByProductIdTest {

		@DisplayName("경매 단건 조회 성공")
		@Test
		void success_findAuction() {
			// given

			// when

			// then

		}

		@DisplayName("경매 단건 조회 실패 - 경매 없음")
		@Test
		void fail_findAuction_notFound() {
			// given

			// when

			// then

		}

	}

	@Nested
	@DisplayName("경매에 DB 존재 여부 확인 테스트")
	class ExistsAuctionTest {

		@DisplayName("확인 성공")
		@Test
		void success_existsAuctionByIdElseThrow() {
			// given

			// when

			// then

		}

		@DisplayName("확인 실패 - 경매 없음")
		@Test
		void fail_existsAuctionByIdElseThrow_notFound() {
			// given

			// when

			// then
		}

	}

	@DisplayName("DB에 종료되지 않는 경매 리스트 조회 테스트")
	@Nested
	class FindActiveAuctionTest {

		@DisplayName("조회 성공")
		@Test
		void success_findActiveAuctionForRecovery() {
			// given

			// when

			// then
		}

	}

	@DisplayName("Redis의 경매 현재가 RDB 백업 테스트")
	@Nested
	class UpdateCurrentPriceBackUpTest {

		@DisplayName("저장 성공")
		@Test
		void success_updateCurrentPriceBackUp() {
			// given

			// when

			// then
		}
	}

	@DisplayName("단순 경매 조회 테스트")
	@Nested
	class FindAuctionTest {

		@DisplayName("경매 조회 성공")
		@Test
		void success_findByAuctionId() {
			// given

			// when

			// then
		}
	}
}
