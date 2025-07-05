package nbc.chillguys.nebulazone.application.bid.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayName("입찰 레디스 서비스 단위 테스트")
@ExtendWith(MockitoExtension.class)
class BidRedisServiceTest {

	@DisplayName("입찰 생성 테스트")
	@Nested
	class CreatedBidTest {

		@DisplayName("입찰 생성 성공")
		@Test
		void success_createBid() {
			// given

			// when

			// then
		}

		@DisplayName("입찰 생성 실패 - 종료된 경매")
		@Test
		void fail_createBid_auctionClosed() {
			// given

			// when

			// then
		}

		@DisplayName("입찰 생성 실패 - 낙찰된 경매")
		@Test
		void fail_createBid_wonAuction() {
			// given

			// when

			// then

		}

		@DisplayName("입찰 생성 실패 - 경매 생성자는 입찰 불가")
		@Test
		void fail_createBid_BidOwnerIsAuctionOwner() {
			// given

			// when

			// then
		}

		@DisplayName("입찰 생성 실패 - 최초 입찰 시 시작가보다 낮은 금액")
		@Test
		void fail_createBid_BidPriceBelowStartPrice() {
			// given

			// when

			// then
		}

		@DisplayName("입찰 생성 실패 - 현재 경매 입찰가보다 낮은 금액")
		@Test
		void fail_createBid_BidPriceBelowCurrentPrice() {
			// given

			// when

			// then
		}
	}

	@Nested
	@DisplayName("입찰 취소 테스트")
	class CancelBidTest {

		@DisplayName("입찰 취소 성공")
		@Test
		void success_cancelBid() {
			// given

			// when

			// then
		}

		@DisplayName("입찰 취소 실패 - 이미 종료된 경매")
		@Test
		void fail_cancelBid_auctionClosed() {
			// given

			// when

			// then

		}

		@DisplayName("입찰 취소 실패 - 낙찰된 경매")
		@Test
		void fail_cancelBid_wonAuction() {
			// given

			// when

			// then

		}

		@DisplayName("입찰 취소 실패 - 경매 종료 30분 전")
		@Test
		void fail_cancelBid_before30Minutes() {
			// given

			// when

			// then

		}

		@DisplayName("입찰 취소 실패 - 입찰 생성자가 아님")
		@Test
		void fail_cancelBid_notBidOwner() {
			// given

			// when

			// then
		}

		@DisplayName("입찰 취소 실패 - 이미 취소된 입찰")
		@Test
		void fail_cancelBid_statusIsCancel() {
			// given

			// when

			// then
		}

		@DisplayName("입찰 취소 실패 - 낙찰된 입찰")
		@Test
		void fail_cancelBid_wonBid() {
			// given

			// when

			// then
		}

		@DisplayName("입찰 취소 실패 - 다른 경매의 입찰 취소")
		@Test
		void fail_cancelBid_mismatchAuction() {
			// given

			// when

			// then
		}
	}

	@Nested
	@DisplayName("특정 경매의 입찰 내역 조회 테스트")
	class FindBidsByAuctionIdTest {

		@DisplayName("조회 성공")
		@Test
		void success_findBidsByAuctionId() {
			// given

			// when

			// then
		}

		@DisplayName("조회 성공 - 데이터 없음")
		@Test
		void success_findBidsByAuctionId_empty() {
			// given

			// when

			// then
		}
	}

	@Nested
	@DisplayName("내 입찰 내역 전체 조회 테스트")
	class FindMyBidsTest {

		@DisplayName("조회 성공")
		@Test
		void success_findMyBidVoList() {
			// given

			// when

			// then
		}

		@DisplayName("조회 성공 - 데이터 없음")
		@Test
		void success_findMyBidVoList_empty() {
			// given

			// when

			// then
		}
	}

	@Nested
	@DisplayName("Redis에 저장된 낙찰 예정인 입찰 정보 조회 테스트")
	class FindWonBidVo {

		@DisplayName("조회 성공")
		@Test
		void success_findWonBidVo() {
			// given

			// when

			// then
		}

		@DisplayName("조회 성공 - 데이터 없음")
		@Test
		void fail_findWonBidVo_empty() {
			// given

			// when

			// then
			// null 처리
		}
	}
}
