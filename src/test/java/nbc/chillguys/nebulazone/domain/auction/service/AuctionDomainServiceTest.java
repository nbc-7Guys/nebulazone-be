package nbc.chillguys.nebulazone.domain.auction.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import nbc.chillguys.nebulazone.domain.auction.dto.AuctionCreateCommand;
import nbc.chillguys.nebulazone.domain.auction.dto.AuctionFindDetailInfo;
import nbc.chillguys.nebulazone.domain.auction.entity.Auction;
import nbc.chillguys.nebulazone.domain.auction.exception.AuctionErrorCode;
import nbc.chillguys.nebulazone.domain.auction.exception.AuctionException;
import nbc.chillguys.nebulazone.domain.auction.repository.AuctionRepository;
import nbc.chillguys.nebulazone.domain.product.entity.Product;
import nbc.chillguys.nebulazone.domain.user.entity.User;

@DisplayName("경매 도메인 서비스 단위 테스트")
@ExtendWith(MockitoExtension.class)
class AuctionDomainServiceTest {

	@Mock
	private AuctionRepository auctionRepository;

	@InjectMocks
	private AuctionDomainService auctionDomainService;

	private Product product;
	private User user;
	private Auction auction;
	private AuctionFindDetailInfo auctionFindDetailInfo;

	@BeforeEach
	void init() {
		user = User.builder()
			.email("seller@test.com")
			.nickname("판매자닉네임")
			.build();
		ReflectionTestUtils.setField(user, "id", 1L);

		product = Product.builder()
			.name("테스트 상품")
			.description("테스트 상품 설명")
			.price(100000L)
			.seller(user)
			.build();
		ReflectionTestUtils.setField(product, "id", 1L);

		auction = Auction.builder()
			.product(product)
			.startPrice(100000L)
			.currentPrice(0L)
			.endTime(LocalDateTime.now().plusDays(1))
			.isWon(false)
			.build();
		ReflectionTestUtils.setField(auction, "id", 1L);

		auctionFindDetailInfo = new AuctionFindDetailInfo(
			1L, 1L, "판매자닉네임", "seller@test.com",
			100000L, 120000L, false, LocalDateTime.now().plusDays(1),
			1L, "테스트 상품", List.of("image1.jpg"), LocalDateTime.now(), 5L);
	}

	@Nested
	@DisplayName("경매 생성 테스트")
	class CreateAuctionTest {

		@DisplayName("경매 생성 성공")
		@Test
		void success_createAuction() {
			// given
			LocalDateTime endTime = LocalDateTime.now().plusDays(1);
			AuctionCreateCommand command = new AuctionCreateCommand(product, endTime);

			given(auctionRepository.save(any(Auction.class))).willReturn(auction);

			// when
			Auction result = auctionDomainService.createAuction(command);

			// then
			assertThat(result.getProduct()).isEqualTo(product);
			assertThat(result.getStartPrice()).isEqualTo(product.getPrice());
			assertThat(result.getCurrentPrice()).isEqualTo(0L);
			assertThat(result.getEndTime()).isEqualToIgnoringNanos(endTime);

			verify(auctionRepository, times(1)).save(any(Auction.class));
		}

	}

	@Nested
	@DisplayName("경매 상세 조회 테스트")
	class FindAuctionDetailTest {

		@DisplayName("경매 상세 조회 성공")
		@Test
		void success_findAuctionDetailInfoByAuctionId() {
			// given
			Long auctionId = 1L;

			given(auctionRepository.findAuctionDetail(auctionId))
				.willReturn(Optional.of(auctionFindDetailInfo));

			// when
			AuctionFindDetailInfo result = auctionDomainService.findAuctionDetailInfoByAuctionId(auctionId);

			// then
			assertThat(result.auctionId()).isEqualTo(auctionId);
			assertThat(result.sellerNickname()).isEqualTo("판매자닉네임");
			assertThat(result.productName()).isEqualTo("테스트 상품");
			assertThat(result.startPrice()).isEqualTo(100000L);
			assertThat(result.currentPrice()).isEqualTo(120000L);

			verify(auctionRepository, times(1)).findAuctionDetail(auctionId);
		}

		@DisplayName("경매 상세 조회 실패 - 경매 없음")
		@Test
		void fail_findAuctionDetailInfoByAuctionId_notFound() {
			// given
			Long auctionId = 999L;

			given(auctionRepository.findAuctionDetail(auctionId))
				.willReturn(Optional.empty());

			// when & then
			assertThatThrownBy(() -> auctionDomainService.findAuctionDetailInfoByAuctionId(auctionId))
				.isInstanceOf(AuctionException.class)
				.hasFieldOrPropertyWithValue("errorCode", AuctionErrorCode.AUCTION_NOT_FOUND);

			verify(auctionRepository, times(1)).findAuctionDetail(auctionId);
		}

	}

	@Nested
	@DisplayName("경매 수동 낙찰 테스트")
	class ManualEndAuctionTest {

		@DisplayName("경매 수동 낙찰 성공")
		@Test
		void success_manualEndAuction() {
			// given
			Long auctionId = 1L;
			Long bidPrice = 120000L;

			given(auctionRepository.findAuctionWithProductAndSeller(auctionId))
				.willReturn(Optional.of(auction));

			// when
			Auction result = auctionDomainService.manualEndAuction(auctionId, bidPrice);

			// then
			assertThat(result).isEqualTo(auction);
			assertThat(result.getCurrentPrice()).isEqualTo(bidPrice);
			assertThat(result.isWon()).isTrue();

			verify(auctionRepository, times(1)).findAuctionWithProductAndSeller(auctionId);
		}

		@DisplayName("경매 수동 낙찰 실패 - 삭제된 경매")
		@Test
		void fail_manualEndAuction_alreadyDeleted() {
			// given
			Long auctionId = 999L;
			Long bidPrice = 120000L;

			given(auctionRepository.findAuctionWithProductAndSeller(auctionId))
				.willReturn(Optional.empty());

			// when & then
			assertThatThrownBy(() -> auctionDomainService.manualEndAuction(auctionId, bidPrice))
				.isInstanceOf(AuctionException.class)
				.hasFieldOrPropertyWithValue("errorCode", AuctionErrorCode.AUCTION_NOT_FOUND);

			verify(auctionRepository, times(1)).findAuctionWithProductAndSeller(auctionId);
		}

	}

	@Nested
	@DisplayName("경매 삭제 테스트")
	class DeleteAuctionTest {

		@DisplayName("경매 삭제 성공")
		@Test
		void success_deleteAuction() {
			// given
			Long auctionId = 1L;

			given(auctionRepository.findByAuctionWithProduct(auctionId))
				.willReturn(Optional.of(auction));

			// when
			Auction result = auctionDomainService.deleteAuction(auctionId);

			// then
			assertThat(result).isEqualTo(auction);
			assertThat(result.isDeleted()).isTrue();

			verify(auctionRepository, times(1)).findByAuctionWithProduct(auctionId);
		}

		@DisplayName("경매 삭제 실패 - 경매 없음")
		@Test
		void fail_deleteAuction_notFound() {
			// given
			Long auctionId = 999L;

			given(auctionRepository.findByAuctionWithProduct(auctionId))
				.willReturn(Optional.empty());

			// when & then
			assertThatThrownBy(() -> auctionDomainService.deleteAuction(auctionId))
				.isInstanceOf(AuctionException.class)
				.hasFieldOrPropertyWithValue("errorCode", AuctionErrorCode.AUCTION_NOT_FOUND);

			verify(auctionRepository, times(1)).findByAuctionWithProduct(auctionId);
		}
	}

	@Nested
	@DisplayName("상품 아이디로 경매 단건 조회 테스트")
	class FindAuctionByProductIdTest {

		@DisplayName("경매 단건 조회 성공")
		@Test
		void success_findAuction() {
			// given
			Long productId = 1L;

			given(auctionRepository.findByProduct_IdAndDeletedFalse(productId))
				.willReturn(Optional.of(auction));

			// when
			Auction result = auctionDomainService.findAuctionByProductId(productId);

			// then
			assertThat(result).isEqualTo(auction);
			assertThat(result.getProduct().getId()).isEqualTo(productId);

			verify(auctionRepository, times(1)).findByProduct_IdAndDeletedFalse(productId);
		}

		@DisplayName("경매 단건 조회 실패 - 경매 없음")
		@Test
		void fail_findAuction_notFound() {
			// given
			Long productId = 999L;

			given(auctionRepository.findByProduct_IdAndDeletedFalse(productId))
				.willReturn(Optional.empty());

			// when & then
			assertThatThrownBy(() -> auctionDomainService.findAuctionByProductId(productId))
				.isInstanceOf(AuctionException.class)
				.hasFieldOrPropertyWithValue("errorCode", AuctionErrorCode.AUCTION_NOT_FOUND);

			verify(auctionRepository, times(1)).findByProduct_IdAndDeletedFalse(productId);
		}

	}

	@Nested
	@DisplayName("경매에 DB 존재 여부 확인 테스트")
	class ExistsAuctionTest {

		@DisplayName("확인 성공")
		@Test
		void success_existsAuctionByIdElseThrow() {
			// given
			Long auctionId = 1L;

			given(auctionRepository.existsById(auctionId)).willReturn(true);

			// when & then
			assertThatCode(() -> auctionDomainService.existsAuctionByIdElseThrow(auctionId))
				.doesNotThrowAnyException();

			verify(auctionRepository, times(1)).existsById(auctionId);
		}

		@DisplayName("확인 실패 - 경매 없음")
		@Test
		void fail_existsAuctionByIdElseThrow_notFound() {
			// given
			Long auctionId = 999L;

			given(auctionRepository.existsById(auctionId)).willReturn(false);

			// when & then
			assertThatThrownBy(() -> auctionDomainService.existsAuctionByIdElseThrow(auctionId))
				.isInstanceOf(AuctionException.class)
				.hasFieldOrPropertyWithValue("errorCode", AuctionErrorCode.AUCTION_NOT_FOUND);

			verify(auctionRepository, times(1)).existsById(auctionId);
		}

	}

	@DisplayName("DB에 종료되지 않는 경매 리스트 조회 테스트")
	@Nested
	class FindActiveAuctionTest {

		@DisplayName("조회 성공")
		@Test
		void success_findActiveAuctionForRecovery() {
			// given
			List<Auction> activeAuctions = List.of(auction);

			given(auctionRepository.findActiveAuctionsForRecovery()).willReturn(activeAuctions);

			// when
			List<Auction> result = auctionDomainService.findActiveAuctionsForRecovery();

			// then
			assertThat(result).hasSize(1);
			assertThat(result.get(0)).isEqualTo(auction);

			verify(auctionRepository, times(1)).findActiveAuctionsForRecovery();
		}

	}

	@DisplayName("Redis의 경매 현재가 RDB 백업 테스트")
	@Nested
	class UpdateCurrentPriceBackUpTest {

		@DisplayName("저장 성공")
		@Test
		void success_updateCurrentPriceBackUp() {
			// given
			Long auctionId = 1L;
			Long currentPrice = 150000L;

			willDoNothing().given(auctionRepository).updateCurrentPriceForBackup(auctionId, currentPrice);

			// when
			auctionDomainService.updateCurrentPriceForBackup(auctionId, currentPrice);

			// then
			verify(auctionRepository, times(1)).updateCurrentPriceForBackup(auctionId, currentPrice);
		}
	}

	@DisplayName("단순 경매 조회 테스트")
	@Nested
	class FindAuctionTest {

		@DisplayName("경매 조회 성공")
		@Test
		void success_findByAuctionId() {
			// given
			Long auctionId = 1L;

			given(auctionRepository.findById(auctionId)).willReturn(Optional.of(auction));

			// when
			Auction result = auctionDomainService.findByAuctionId(auctionId);

			// then
			assertThat(result).isEqualTo(auction);
			assertThat(result.getId()).isEqualTo(auctionId);

			verify(auctionRepository, times(1)).findById(auctionId);
		}
	}
}
