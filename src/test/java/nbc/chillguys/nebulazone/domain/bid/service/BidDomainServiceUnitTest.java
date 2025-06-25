package nbc.chillguys.nebulazone.domain.bid.service;

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

import nbc.chillguys.nebulazone.domain.auction.entity.Auction;
import nbc.chillguys.nebulazone.domain.auction.exception.AuctionErrorCode;
import nbc.chillguys.nebulazone.domain.auction.exception.AuctionException;
import nbc.chillguys.nebulazone.domain.bid.dto.FindBidInfo;
import nbc.chillguys.nebulazone.domain.bid.entity.Bid;
import nbc.chillguys.nebulazone.domain.bid.entity.BidStatus;
import nbc.chillguys.nebulazone.domain.bid.exception.BidErrorCode;
import nbc.chillguys.nebulazone.domain.bid.exception.BidException;
import nbc.chillguys.nebulazone.domain.bid.repository.BidRepository;
import nbc.chillguys.nebulazone.domain.catalog.entity.Catalog;
import nbc.chillguys.nebulazone.domain.catalog.entity.CatalogType;
import nbc.chillguys.nebulazone.domain.product.entity.Product;
import nbc.chillguys.nebulazone.domain.product.entity.ProductTxMethod;
import nbc.chillguys.nebulazone.domain.user.entity.OAuthType;
import nbc.chillguys.nebulazone.domain.user.entity.User;
import nbc.chillguys.nebulazone.domain.user.entity.UserRole;

@DisplayName("입찰 도메인 서비스 단위 테스트")
@ExtendWith(MockitoExtension.class)
class BidDomainServiceUnitTest {

	@Mock
	BidRepository bidRepository;

	@InjectMocks
	BidDomainService bidDomainService;

	private static final String SELLER_EMAIL = "seller@test.com";
	private static final String SELLER_NICKNAME = "판매자";
	private static final String BIDDER_EMAIL = "bidder@test.com";
	private static final String BIDDER_NICKNAME = "입찰자";
	private static final String OTHER_USER_EMAIL = "other@test.com";
	private static final String OTHER_USER_NICKNAME = "다른사용자";
	private static final String PRODUCT_NAME = "테스트 CPU";
	private static final String PRODUCT_DESCRIPTION = "설명";
	private static final String CATALOG_NAME = "테스트 CPU";
	private static final String CATALOG_DESCRIPTION = "카탈로그";
	private static final Long START_PRICE = 500000L;
	private static final Long CURRENT_PRICE = 600000L;
	private static final Long NEW_BID_PRICE = 650000L;
	private static final Long LOW_BID_PRICE = 550000L;

	private User seller;
	private User bidder;
	private User otherUser;
	private Catalog catalog;
	private Product product;
	private Auction auction;

	@BeforeEach
	void setUp() {
		seller = createUser(1L, SELLER_EMAIL, SELLER_NICKNAME);
		bidder = createUser(2L, BIDDER_EMAIL, BIDDER_NICKNAME);
		otherUser = createUser(3L, OTHER_USER_EMAIL, OTHER_USER_NICKNAME);
		catalog = createCatalog(1L, CATALOG_NAME, CATALOG_DESCRIPTION);
		product = createProduct(1L, PRODUCT_NAME, PRODUCT_DESCRIPTION, START_PRICE, seller, catalog);
		auction = createAuction(1L, product, START_PRICE, CURRENT_PRICE,
			LocalDateTime.now().plusDays(1), false, false);
	}

	@Nested
	@DisplayName("입찰 생성")
	class CreateBidTest {

		@Test
		@DisplayName("입찰 생성 성공")
		void success_createBid() {
			// 수정해야함

		}

		@Test
		@DisplayName("입찰 생성 실패 - 경매 시작가보다 입찰가가 낮음")
		void fail_createBid_whenBidPriceLowerThanStartPrice() {
			// given

			// when

			// then
		}

		@Test
		@DisplayName("입찰 생성 실패 - 경매 종료")
		void fail_createBid_auctionClosed() {
			// given
			Auction closedAuction = createAuction(2L, product, START_PRICE, CURRENT_PRICE,
				LocalDateTime.now().minusHours(1), false, false);

			// when & then
			assertAuctionException(() -> bidDomainService.createBid(closedAuction, bidder, NEW_BID_PRICE),
				AuctionErrorCode.ALREADY_CLOSED_AUCTION);
		}

		@Test
		@DisplayName("입찰 생성 실패 - 이미 낙찰된 경매")
		void fail_createBid_alreadyWon() {
			// given
			Auction wonAuction = createAuction(3L, product, START_PRICE, CURRENT_PRICE,
				LocalDateTime.now().plusDays(1), false, true);

			// when & then
			assertAuctionException(() -> bidDomainService.createBid(wonAuction, bidder, NEW_BID_PRICE),
				AuctionErrorCode.ALREADY_WON_AUCTION);
		}

		@Test
		@DisplayName("입찰 생성 실패 - 경매 소유자가 입찰")
		void fail_createBid_ownerBid() {
			// when & then
			assertBidException(() -> bidDomainService.createBid(auction, seller, NEW_BID_PRICE),
				BidErrorCode.CANNOT_BID_OWN_AUCTION);
		}

		@Test
		@DisplayName("입찰 생성 실패 - 입찰가가 현재가보다 낮음")
		void fail_createBid_lowPrice() {
			// given
			given(bidRepository.findActiveBidHighestPriceByAuction(auction))
				.willReturn(Optional.of(CURRENT_PRICE));

			// when & then
			assertBidException(() -> bidDomainService.createBid(auction, bidder, LOW_BID_PRICE),
				BidErrorCode.BID_PRICE_TOO_LOW_CURRENT_PRICE);
		}

		@Test
		@DisplayName("입찰 생성 실패 - 입찰가가 동일함")
		void fail_createBid_samePrice() {
			// given
			given(bidRepository.findActiveBidHighestPriceByAuction(auction))
				.willReturn(Optional.of(CURRENT_PRICE));

			// when & then
			assertBidException(() -> bidDomainService.createBid(auction, bidder, CURRENT_PRICE),
				BidErrorCode.BID_PRICE_TOO_LOW_CURRENT_PRICE);
		}
	}

	@Nested
	@DisplayName("입찰 수정")
	class UpdateBidTest {

		@Test
		@DisplayName("입찰 수정 성공")
		void success_updateBid() {
			// given

			// when

			// then
		}

	}

	@Nested
	@DisplayName("입찰 내역 조회")
	class FindBidsTest {

		@Test
		@DisplayName("특정 경매의 입찰 내역 조회 성공")
		void success_findBids() {
			// given
			int page = 0;
			int size = 10;
			List<FindBidInfo> bidInfoList = createBidInfoList();
			Page<FindBidInfo> pageResult = new PageImpl<>(bidInfoList);

			given(bidRepository.findBidsWithUserByAuction(auction, page, size))
				.willReturn(pageResult);

			// when
			Page<FindBidInfo> result = bidDomainService.findBids(auction, page, size);

			// then
			assertThat(result.getTotalElements()).isEqualTo(2);
			assertThat(result.getContent().get(0).bidPrice()).isEqualTo(NEW_BID_PRICE);
			assertThat(result.getContent().get(0).nickname()).isEqualTo(BIDDER_NICKNAME);
			assertThat(result.getContent().get(1).bidPrice()).isEqualTo(CURRENT_PRICE);
		}

		@Test
		@DisplayName("내 입찰 내역 조회 성공")
		void success_findMyBids() {
			// given
			int page = 0;
			int size = 10;
			List<FindBidInfo> myBidInfoList = List.of(
				new FindBidInfo(1L, NEW_BID_PRICE, LocalDateTime.now(), BidStatus.BID,
					BIDDER_NICKNAME, PRODUCT_NAME)
			);
			Page<FindBidInfo> pageResult = new PageImpl<>(myBidInfoList);

			given(bidRepository.findMyBids(bidder, page, size))
				.willReturn(pageResult);

			// when
			Page<FindBidInfo> result = bidDomainService.findMyBids(bidder, page, size);

			// then
			assertThat(result.getTotalElements()).isEqualTo(1);
			assertThat(result.getContent().get(0).nickname()).isEqualTo(BIDDER_NICKNAME);
			assertThat(result.getContent().get(0).bidPrice()).isEqualTo(NEW_BID_PRICE);
		}
	}

	@Nested
	@DisplayName("최고가 입찰 조회")
	class FindHighBidByAuctionTest {

		@Test
		@DisplayName("최고가 입찰 조회 성공")
		void success_findHighBidByAuction() {
			// given
			Long auctionId = 200L;
			Bid highestBid = createBid(500L, auction, bidder, NEW_BID_PRICE, BidStatus.BID);

			given(bidRepository.findHighestPriceBidByAuctionWithUser(auctionId))
				.willReturn(highestBid);

			// when
			Bid result = bidDomainService.findHighBidByAuction(auctionId);

			// then
			assertThat(result).isEqualTo(highestBid);
			assertThat(result.getPrice()).isEqualTo(NEW_BID_PRICE);
			assertThat(result.getUser()).isEqualTo(bidder);
		}
	}

	@Nested
	@DisplayName("입찰 단건 조회")
	class FindBidTest {

		@Test
		@DisplayName("입찰 조회 성공")
		void success_findBid() {
			// given
			Long bidId = 300L;
			Bid bid = createBid(bidId, auction, bidder, NEW_BID_PRICE, BidStatus.BID);

			given(bidRepository.findBidWithWonUser(bidId)).willReturn(Optional.of(bid));

			// when
			Bid result = bidDomainService.findBid(bidId);

			// then
			assertThat(result).isEqualTo(bid);
			assertThat(result.getId()).isEqualTo(bidId);
			assertThat(result.getPrice()).isEqualTo(NEW_BID_PRICE);
		}

		@Test
		@DisplayName("입찰 조회 실패 - 존재하지 않는 입찰")
		void fail_findBid_notFound() {
			// given
			Long bidId = 999L;
			given(bidRepository.findBidWithWonUser(bidId)).willReturn(Optional.empty());

			// when & then
			assertBidException(() -> bidDomainService.findBid(bidId),
				BidErrorCode.BID_NOT_FOUND);
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

	private Bid createBid(Long id, Auction auction, User user, Long price, BidStatus status) {
		Bid bid = Bid.builder()
			.auction(auction)
			.user(user)
			.price(price)
			.build();
		ReflectionTestUtils.setField(bid, "id", id);
		ReflectionTestUtils.setField(bid, "status", status);
		return bid;
	}

	private List<FindBidInfo> createBidInfoList() {
		return List.of(
			new FindBidInfo(1L, NEW_BID_PRICE, LocalDateTime.now(), BidStatus.BID,
				BIDDER_NICKNAME, PRODUCT_NAME),
			new FindBidInfo(2L, CURRENT_PRICE, LocalDateTime.now().minusHours(1), BidStatus.BID,
				OTHER_USER_NICKNAME, PRODUCT_NAME)
		);
	}

	// 공통 예외 검증 헬퍼 메서드
	private void assertBidException(Runnable executable, BidErrorCode expectedErrorCode) {
		assertThatThrownBy(executable::run)
			.isInstanceOf(BidException.class)
			.extracting("errorCode")
			.isEqualTo(expectedErrorCode);
	}

	private void assertAuctionException(Runnable executable, AuctionErrorCode expectedErrorCode) {
		assertThatThrownBy(executable::run)
			.isInstanceOf(AuctionException.class)
			.extracting("errorCode")
			.isEqualTo(expectedErrorCode);
	}
}
