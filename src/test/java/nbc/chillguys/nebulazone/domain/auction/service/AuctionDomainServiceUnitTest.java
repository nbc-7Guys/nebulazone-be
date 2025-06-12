package nbc.chillguys.nebulazone.domain.auction.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.util.ReflectionTestUtils;

import nbc.chillguys.nebulazone.domain.auction.dto.AuctionCreateCommand;
import nbc.chillguys.nebulazone.domain.auction.dto.AuctionFindAllInfo;
import nbc.chillguys.nebulazone.domain.auction.dto.AuctionFindDetailInfo;
import nbc.chillguys.nebulazone.domain.auction.dto.ManualEndAuctionInfo;
import nbc.chillguys.nebulazone.domain.auction.entity.Auction;
import nbc.chillguys.nebulazone.domain.auction.entity.AuctionSortType;
import nbc.chillguys.nebulazone.domain.auction.exception.AuctionErrorCode;
import nbc.chillguys.nebulazone.domain.auction.exception.AuctionException;
import nbc.chillguys.nebulazone.domain.auction.repository.AuctionRepository;
import nbc.chillguys.nebulazone.domain.bid.entity.Bid;
import nbc.chillguys.nebulazone.domain.catalog.entity.Catalog;
import nbc.chillguys.nebulazone.domain.catalog.entity.CatalogType;
import nbc.chillguys.nebulazone.domain.products.entity.Product;
import nbc.chillguys.nebulazone.domain.products.entity.ProductTxMethod;
import nbc.chillguys.nebulazone.domain.user.entity.OAuthType;
import nbc.chillguys.nebulazone.domain.user.entity.User;
import nbc.chillguys.nebulazone.domain.user.entity.UserRole;

@DisplayName("경매 도메인 서비스 단위 테스트")
@ExtendWith(MockitoExtension.class)
class AuctionDomainServiceUnitTest {

	@Mock
	AuctionRepository auctionRepository;

	@InjectMocks
	AuctionDomainService auctionDomainService;

	private static final String SELLER_EMAIL = "seller@test.com";
	private static final String SELLER_NICKNAME = "판매자";
	private static final String WINNER_EMAIL = "win@test.com";
	private static final String WINNER_NICKNAME = "입찰자";
	private static final String PRODUCT_NAME = "테스트 CPU";
	private static final String PRODUCT_DESCRIPTION = "설명";
	private static final String CATALOG_NAME = "테스트 CPU";
	private static final String CATALOG_DESCRIPTION = "카탈로그";
	private static final Long START_PRICE = 100000L;
	private static final Long CURRENT_PRICE = 150000L;
	private static final Long MISMATCH_PRICE = 140000L;

	// 공통 테스트 픽스처
	private User seller;
	private User winner;
	private User notOwner;
	private Catalog catalog;
	private Product product;

	@BeforeEach
	void setUp() {
		seller = createUser(1L, SELLER_EMAIL, SELLER_NICKNAME);
		winner = createUser(2L, WINNER_EMAIL, WINNER_NICKNAME);
		notOwner = createUser(3L, "other@test.com", "다른사람");
		catalog = createCatalog(1L, CATALOG_NAME, CATALOG_DESCRIPTION);
		product = createProduct(1L, PRODUCT_NAME, PRODUCT_DESCRIPTION, START_PRICE, seller, catalog);
	}

	@Nested
	@DisplayName("경매 생성")
	class CreateAuctionTest {
		@Test
		@DisplayName("경매 생성 성공")
		void createAuction_success() {
			// given
			LocalDateTime endTime = LocalDateTime.of(2024, 12, 31, 23, 59, 59);
			AuctionCreateCommand command = new AuctionCreateCommand(product, endTime);
			Auction auction = createAuction(1L, product, START_PRICE, START_PRICE, endTime, false, false);

			given(auctionRepository.save(any(Auction.class))).willReturn(auction);

			// when
			Auction result = auctionDomainService.createAuction(command);

			// then
			assertThat(result.getProduct().getName()).isEqualTo(PRODUCT_NAME);
			assertThat(result.getStartPrice()).isEqualTo(START_PRICE);
			assertThat(result.getEndTime()).isEqualTo(endTime);
			assertThat(result.isWon()).isFalse();
		}
	}

	@Nested
	@DisplayName("경매 조회")
	class FindAuctionTest {

		@Test
		@DisplayName("경매 전체 조회(페이징) 성공")
		void findAuctions_success() {
			// given
			int page = 0;
			int size = 2;
			List<AuctionFindAllInfo> content = createAuctionFindAllInfoList();
			Page<AuctionFindAllInfo> pageResult = new PageImpl<>(content);

			given(auctionRepository.findAuctionsWithProduct(page, size)).willReturn(pageResult);

			// when
			Page<AuctionFindAllInfo> result = auctionDomainService.findAuctions(page, size);

			// then
			assertThat(result.getTotalElements()).isEqualTo(2);
			assertThat(result.getContent().get(0).productName()).isEqualTo(PRODUCT_NAME);
			assertThat(result.getContent().get(1).bidCount()).isEqualTo(5L);
		}

		@Test
		@DisplayName("인기 경매 조회 성공")
		void findAuctionsBySortType_success_popular() {
			// given
			AuctionFindAllInfo info = createAuctionFindAllInfo(1L, START_PRICE, 110000L, 10L);
			given(auctionRepository.finAuctionsBySortType(AuctionSortType.POPULAR)).willReturn(List.of(info));

			// when
			List<AuctionFindAllInfo> result = auctionDomainService.findAuctionsBySortType(AuctionSortType.POPULAR);

			// then
			assertThat(result).hasSize(1);
			assertThat(result.get(0).productName()).isEqualTo(PRODUCT_NAME);
		}

		@Test
		@DisplayName("마감 임박순 경매 조회 성공")
		void findAuctionsBySortType_success_closing() {
			// given
			AuctionFindAllInfo info = createAuctionFindAllInfo(2L, 120000L, 125000L, 3L);
			given(auctionRepository.finAuctionsBySortType(AuctionSortType.CLOSING)).willReturn(List.of(info));

			// when
			List<AuctionFindAllInfo> result = auctionDomainService.findAuctionsBySortType(AuctionSortType.CLOSING);

			// then
			assertThat(result).hasSize(1);
			assertThat(result.get(0).bidCount()).isEqualTo(3L);
		}

		@Test
		@DisplayName("경매 상세 조회 성공")
		void findAuction_success() {
			// given
			Long auctionId = 11L;
			AuctionFindDetailInfo detailInfo = createAuctionFindDetailInfo(auctionId);
			given(auctionRepository.findAuctionDetail(auctionId)).willReturn(Optional.of(detailInfo));

			// when
			AuctionFindDetailInfo result = auctionDomainService.findAuction(auctionId);

			// then
			assertThat(result.auctionId()).isEqualTo(auctionId);
			assertThat(result.productName()).isEqualTo(PRODUCT_NAME);
			assertThat(result.sellerNickname()).isEqualTo(SELLER_NICKNAME);
		}

		@Test
		@DisplayName("경매 상세 조회 실패 - 경매 없음")
		void findAuction_fail_notFound() {
			// given
			Long auctionId = 99L;
			given(auctionRepository.findAuctionDetail(auctionId)).willReturn(Optional.empty());

			// when & then
			assertAuctionException(() -> auctionDomainService.findAuction(auctionId),
				AuctionErrorCode.AUCTION_NOT_FOUND);
		}
	}

	@Nested
	@DisplayName("수동 낙찰")
	class ManualEndAuctionTest {

		@Test
		@DisplayName("수동 낙찰 성공")
		void manualEndAuction_success() {
			// given
			Long auctionId = 1001L;
			Auction auction = createAuction(auctionId, product, START_PRICE, CURRENT_PRICE,
				LocalDateTime.now().plusDays(1), false, false);
			Bid bid = createBid(9999L, auction, winner, CURRENT_PRICE);

			given(auctionRepository.findById(auctionId)).willReturn(Optional.of(auction));

			// when
			ManualEndAuctionInfo result = auctionDomainService.manualEndAuction(seller, bid, auctionId);

			// then
			assertThat(result.auctionId()).isEqualTo(auctionId);
			assertThat(result.BidId()).isEqualTo(9999L);
			assertThat(result.winnerNickname()).isEqualTo(WINNER_NICKNAME);
			assertThat(result.wonProductPrice()).isEqualTo(CURRENT_PRICE);
		}

		@Test
		@DisplayName("수동 낙찰 실패 - 존재하지 않는 경매")
		void manualEndAuction_fail_notFound() {
			// given
			Long auctionId = 2002L;
			Bid bid = mock(
				Bid.class);
			given(auctionRepository.findById(auctionId)).willReturn(Optional.empty());

			// when & then
			assertAuctionException(() -> auctionDomainService.manualEndAuction(seller, bid, auctionId),
				AuctionErrorCode.AUCTION_NOT_FOUND);
		}

		@Test
		@DisplayName("수동 낙찰 실패 - 이미 삭제된 경매")
		void manualEndAuction_fail_deleted() {
			// given
			Long auctionId = 2003L;
			Auction auction = createDeletedAuction(auctionId, product, START_PRICE, CURRENT_PRICE);
			Bid bid = mock(
				Bid.class);

			given(auctionRepository.findById(auctionId)).willReturn(Optional.of(auction));

			// when & then
			assertAuctionException(() -> auctionDomainService.manualEndAuction(seller, bid, auctionId),
				AuctionErrorCode.ALREADY_DELETED_AUCTION);
		}

		@Test
		@DisplayName("수동 낙찰 실패 - 경매 작성자가 아님")
		void manualEndAuction_fail_notOwner() {
			// given
			Long auctionId = 2004L;
			Auction auction = createAuction(auctionId, product, START_PRICE, CURRENT_PRICE,
				LocalDateTime.now().plusDays(1), false, false);
			Bid bid = mock(
				Bid.class);

			given(auctionRepository.findById(auctionId)).willReturn(Optional.of(auction));

			// when & then
			assertAuctionException(() -> auctionDomainService.manualEndAuction(notOwner, bid, auctionId),
				AuctionErrorCode.AUCTION_NOT_OWNER);
		}

		@Test
		@DisplayName("수동 낙찰 실패 - 요청된 입찰가와 경매 현재가 불일치")
		void manualEndAuction_fail_mismatch_bidPrice() {
			// given
			Long auctionId = 2005L;
			Auction auction = createAuction(auctionId, product, START_PRICE, CURRENT_PRICE,
				LocalDateTime.now().plusDays(1), false, false);
			Bid bid = createBid(9998L, auction, winner, MISMATCH_PRICE);

			given(auctionRepository.findById(auctionId)).willReturn(Optional.of(auction));

			// when & then
			assertAuctionException(() -> auctionDomainService.manualEndAuction(seller, bid, auctionId),
				AuctionErrorCode.MISMATCH_BID_PRICE);
		}
	}

	@Nested
	@DisplayName("경매 삭제")
	class DeleteAuctionTest {

		@Test
		@DisplayName("경매 삭제 성공")
		void deleteAuction_success() {
			// given
			Long auctionId = 101L;
			Auction auction = createAuction(auctionId, product, START_PRICE, START_PRICE,
				LocalDateTime.of(2025, 12, 31, 23, 59, 59), false, false);

			given(auctionRepository.findById(auctionId)).willReturn(Optional.of(auction));

			// when
			auctionDomainService.deleteAuction(auctionId, seller);

			// then
			assertThat(auction.isDeleted()).isTrue();
			assertThat(auction.getDeletedAt()).isNotNull();
		}

		@Test
		@DisplayName("경매 삭제 실패 - 존재하지 않는 경매")
		void deleteAuction_fail_notFound() {
			// given
			Long auctionId = 101L;
			given(auctionRepository.findById(auctionId)).willReturn(Optional.empty());

			// when & then
			assertAuctionException(() -> auctionDomainService.deleteAuction(auctionId, seller),
				AuctionErrorCode.AUCTION_NOT_FOUND);
		}

		@Test
		@DisplayName("경매 삭제 실패 - 이미 삭제된 경매")
		void deleteAuction_fail_deleted() {
			// given
			Long auctionId = 101L;
			Auction auction = createDeletedAuction(auctionId, product, START_PRICE, START_PRICE);

			given(auctionRepository.findById(auctionId)).willReturn(Optional.of(auction));

			// when & then
			assertAuctionException(() -> auctionDomainService.deleteAuction(auctionId, seller),
				AuctionErrorCode.ALREADY_DELETED_AUCTION);
		}

		@Test
		@DisplayName("경매 삭제 실패 - 경매 소유자 아님")
		void deleteAuction_fail_notOwner() {
			// given
			Long auctionId = 201L;
			Auction auction = createAuction(auctionId, product, START_PRICE, START_PRICE,
				LocalDateTime.of(2025, 12, 31, 23, 59, 59), false, false);

			given(auctionRepository.findById(auctionId)).willReturn(Optional.of(auction));

			// when & then
			assertAuctionException(() -> auctionDomainService.deleteAuction(auctionId, notOwner),
				AuctionErrorCode.AUCTION_NOT_OWNER);
		}
	}

	@Nested
	@DisplayName("특정 상품ID로 경매 조회")
	class FindAuctionByProductIdTest {

		@Test
		@DisplayName("정상 조회")
		void findAuctionByProductId_success() {
			// given
			Long productId = 100L;
			Auction auction = createSimpleAuction(10000L, 12000L, false, false);
			given(auctionRepository.findByProduct_IdAndDeletedFalse(productId)).willReturn(Optional.of(auction));

			// when
			Auction result = auctionDomainService.findAuctionByProductId(productId);

			// then
			assertThat(result.getStartPrice()).isEqualTo(10000L);
			assertThat(result.isWon()).isFalse();
		}

		@Test
		@DisplayName("실패 - 경매 없음")
		void findAuctionByProductId_fail_notFound() {
			// given
			Long productId = 100L;
			given(auctionRepository.findByProduct_IdAndDeletedFalse(productId)).willReturn(Optional.empty());

			// when & then
			assertAuctionException(() -> auctionDomainService.findAuctionByProductId(productId),
				AuctionErrorCode.AUCTION_NOT_FOUND);
		}
	}

	@Nested
	@DisplayName("삭제되지 않은 경매 단건 조회")
	class FindActiveAuctionByIdTest {

		@Test
		@DisplayName("정상 조회")
		void findActiveAuctionById_success() {
			// given
			Long auctionId = 201L;
			Auction auction = createSimpleAuction(20000L, 22000L, false, true);
			given(auctionRepository.findByIdAndDeletedFalse(auctionId)).willReturn(Optional.of(auction));

			// when
			Auction result = auctionDomainService.findActiveAuctionById(auctionId);

			// then
			assertThat(result.isWon()).isTrue();
			assertThat(result.getCurrentPrice()).isEqualTo(22000L);
		}

		@Test
		@DisplayName("실패 - 경매 없음/삭제됨")
		void findActiveAuctionById_fail_notFound() {
			// given
			Long auctionId = 201L;
			given(auctionRepository.findByIdAndDeletedFalse(auctionId)).willReturn(Optional.empty());

			// when & then
			assertAuctionException(() -> auctionDomainService.findActiveAuctionById(auctionId),
				AuctionErrorCode.AUCTION_NOT_FOUND);
		}
	}

	@Nested
	@DisplayName("비관적 락이 적용된 경매 조회")
	class FindActiveAuctionWithProductAndSellerLockTest {

		@Test
		@DisplayName("정상 조회")
		void findActiveAuctionWithProductAndSellerLock_success() {
			// given
			Long auctionId = 301L;
			Auction auction = createSimpleAuction(30000L, 35000L, false, false);
			given(auctionRepository.findAuctionWithProductAndSellerLock(auctionId)).willReturn(Optional.of(auction));

			// when
			Auction result = auctionDomainService.findActiveAuctionWithProductAndSellerLock(auctionId);

			// then
			assertThat(result.getCurrentPrice()).isEqualTo(35000L);
		}

		@Test
		@DisplayName("실패 - 경매 없음")
		void findActiveAuctionWithProductAndSellerLock_fail_notFound() {
			// given
			Long auctionId = 301L;
			given(auctionRepository.findAuctionWithProductAndSellerLock(auctionId)).willReturn(Optional.empty());

			// when & then
			assertAuctionException(() -> auctionDomainService.findActiveAuctionWithProductAndSellerLock(auctionId),
				AuctionErrorCode.AUCTION_NOT_FOUND);
		}
	}

	@Nested
	@DisplayName("삭제되지 않은 경매 리스트 조회")
	class FindActiveAuctionsWithProductAndSellerTest {

		@Test
		@DisplayName("정상 조회")
		void findActiveAuctionsWithProductAndSeller_success() {
			// given
			List<Auction> auctionList = List.of(
				createSimpleAuction(1000L, 1500L, false, false),
				createSimpleAuction(2000L, 2500L, false, true)
			);
			given(auctionRepository.findAuctionsByNotDeletedAndIsWonFalse()).willReturn(auctionList);

			// when
			List<Auction> result = auctionDomainService.findActiveAuctionsWithProductAndSeller();

			// then
			assertThat(result).hasSize(2);
			assertThat(result.get(0).getStartPrice()).isEqualTo(1000L);
			assertThat(result.get(1).isWon()).isTrue();
		}
	}

	// 팩토리 메서드들
	private User createUser(Long id, String email, String nickname) {
		User user = User.builder()
			.email(email)
			.nickname(nickname)
			.oAuthType(OAuthType.DOMAIN)
			.roles(Set.of(UserRole.ROLE_USER))
			.build();
		ReflectionTestUtils.setField(user, "id", id);
		return user;
	}

	private Catalog createCatalog(Long id, String name, String description) {
		Catalog catalog = Catalog.builder()
			.name(name)
			.description(description)
			.type(CatalogType.CPU)
			.build();
		ReflectionTestUtils.setField(catalog, "id", id);
		return catalog;
	}

	private Product createProduct(Long id, String name, String description, Long price, User seller, Catalog catalog) {
		Product product = Product.builder()
			.name(name)
			.description(description)
			.price(price)
			.txMethod(ProductTxMethod.AUCTION)
			.seller(seller)
			.catalog(catalog)
			.build();
		ReflectionTestUtils.setField(product, "id", id);
		return product;
	}

	private Auction createAuction(Long id, Product product, Long startPrice, Long currentPrice,
		LocalDateTime endTime, boolean isDeleted, boolean isWon) {
		Auction auction = Auction.builder()
			.product(product)
			.startPrice(startPrice)
			.currentPrice(currentPrice)
			.endTime(endTime)
			.isDeleted(isDeleted)
			.isWon(isWon)
			.build();
		ReflectionTestUtils.setField(auction, "id", id);
		return auction;
	}

	private Auction createDeletedAuction(Long id, Product product, Long startPrice, Long currentPrice) {
		Auction auction = createAuction(id, product, startPrice, currentPrice,
			LocalDateTime.now().plusDays(1), true, false);
		ReflectionTestUtils.setField(auction, "deletedAt", LocalDateTime.now());
		return auction;
	}

	private Auction createSimpleAuction(Long startPrice, Long currentPrice, boolean isDeleted, boolean isWon) {
		return Auction.builder()
			.product(mock(Product.class))
			.startPrice(startPrice)
			.currentPrice(currentPrice)
			.endTime(LocalDateTime.now().plusDays(1))
			.isDeleted(isDeleted)
			.isWon(isWon)
			.build();
	}

	private Bid createBid(Long id, Auction auction, User user, Long price) {
		Bid bid = Bid.builder()
			.auction(auction)
			.user(user)
			.price(price)
			.build();
		ReflectionTestUtils.setField(bid, "id", id);
		return bid;
	}

	private List<AuctionFindAllInfo> createAuctionFindAllInfoList() {
		AuctionFindAllInfo info1 = createAuctionFindAllInfo(1L, START_PRICE, 110000L, 10L);
		AuctionFindAllInfo info2 = createAuctionFindAllInfo(2L, 120000L, 125000L, 5L);
		return List.of(info1, info2);
	}

	private AuctionFindAllInfo createAuctionFindAllInfo(Long id, Long startPrice, Long currentPrice, Long bidCount) {
		return new AuctionFindAllInfo(id, startPrice, currentPrice, false,
			LocalDateTime.now().plusDays(1), LocalDateTime.now(), PRODUCT_NAME, "img", bidCount);
	}

	private AuctionFindDetailInfo createAuctionFindDetailInfo(Long auctionId) {
		return new AuctionFindDetailInfo(auctionId, 1L, SELLER_NICKNAME, SELLER_EMAIL,
			START_PRICE, 110000L, false, LocalDateTime.now().plusDays(1), 7L, PRODUCT_NAME, "img",
			LocalDateTime.now().minusDays(1), 3L);
	}

	// 공통 예외 검증 헬퍼 메서드
	private void assertAuctionException(Runnable executable, AuctionErrorCode expectedErrorCode) {
		assertThatThrownBy(executable::run)
			.isInstanceOf(AuctionException.class)
			.extracting("errorCode")
			.isEqualTo(expectedErrorCode);
	}
}
