// package nbc.chillguys.nebulazone.domain.auction.service;
//
// import java.time.LocalDateTime;
// import java.util.Set;
//
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.DisplayName;
// import org.junit.jupiter.api.Nested;
// import org.junit.jupiter.api.extension.ExtendWith;
// import org.mockito.InjectMocks;
// import org.mockito.Mock;
// import org.mockito.junit.jupiter.MockitoExtension;
// import org.springframework.test.util.ReflectionTestUtils;
//
// import nbc.chillguys.nebulazone.domain.auction.entity.Auction;
// import nbc.chillguys.nebulazone.domain.auction.repository.AuctionRepository;
// import nbc.chillguys.nebulazone.domain.bid.entity.Bid;
// import nbc.chillguys.nebulazone.domain.catalog.entity.Catalog;
// import nbc.chillguys.nebulazone.domain.catalog.entity.CatalogType;
// import nbc.chillguys.nebulazone.domain.product.entity.Product;
// import nbc.chillguys.nebulazone.domain.product.entity.ProductTxMethod;
// import nbc.chillguys.nebulazone.domain.user.entity.OAuthType;
// import nbc.chillguys.nebulazone.domain.user.entity.User;
// import nbc.chillguys.nebulazone.domain.user.entity.UserRole;
//
// @DisplayName("자동 경매 도메인 서비스 단위 테스트")
// @ExtendWith({MockitoExtension.class})
// class AutoAuctionDomainServiceUnitTest {
//
// 	@Mock
// 	AuctionRepository auctionRepository;
//
// 	@InjectMocks
// 	AutoAuctionDomainService autoAuctionDomainService;
//
// 	private static final String SELLER_EMAIL = "seller@test.com";
// 	private static final String SELLER_NICKNAME = "판매자";
// 	private static final String BUYER_EMAIL = "buyer@test.com";
// 	private static final String BUYER_NICKNAME = "구매자";
// 	private static final String PRODUCT_NAME = "테스트 CPU";
// 	private static final String PRODUCT_DESCRIPTION = "설명";
// 	private static final String CATALOG_NAME = "테스트 CPU";
// 	private static final String CATALOG_DESCRIPTION = "카탈로그";
// 	private static final Long START_PRICE = 100000L;
// 	private static final Long BID_PRICE = 150000L;
//
// 	private User seller;
// 	private User buyer;
// 	private Catalog catalog;
// 	private Product product;
//
// 	@BeforeEach
// 	void setUp() {
//
// 	}
//
// 	@Nested
// 	@DisplayName("자동 경매 종료")
// 	class AutoEndAuctionTest {
// 	}
//
// 	private User createUser(Long id, String email, String nickname) {
// 		User user = User.builder()
// 			.email(email)
// 			.nickname(nickname)
// 			.oAuthType(OAuthType.DOMAIN)
// 			.roles(Set.of(UserRole.ROLE_USER))
// 			.build();
// 		ReflectionTestUtils.setField(user, "id", id);
// 		return user;
// 	}
//
// 	private Catalog createCatalog(Long id, String name, String description) {
// 		Catalog catalog = Catalog.builder()
// 			.name(name)
// 			.description(description)
// 			.type(CatalogType.CPU)
// 			.build();
// 		ReflectionTestUtils.setField(catalog, "id", id);
// 		return catalog;
// 	}
//
// 	private Product createProduct(Long id, String name, String description, Long price, User seller, Catalog catalog) {
// 		Product product = Product.builder()
// 			.name(name)
// 			.description(description)
// 			.price(price)
// 			.txMethod(ProductTxMethod.AUCTION)
// 			.seller(seller)
// 			.catalog(catalog)
// 			.build();
// 		ReflectionTestUtils.setField(product, "id", id);
// 		return product;
// 	}
//
// 	private Auction createAuction(Long id, Product product, Long startPrice, Long currentPrice,
// 		LocalDateTime endTime, boolean isDeleted, boolean isWon) {
// 		Auction auction = Auction.builder()
// 			.product(product)
// 			.startPrice(startPrice)
// 			.currentPrice(currentPrice)
// 			.endTime(endTime)
// 			.isDeleted(isDeleted)
// 			.isWon(isWon)
// 			.build();
// 		ReflectionTestUtils.setField(auction, "id", id);
// 		return auction;
// 	}
//
// 	private Auction createDeletedAuction(Long id, Product product, Long startPrice, Long currentPrice) {
// 		Auction auction = createAuction(id, product, startPrice, currentPrice,
// 			LocalDateTime.now().plusDays(1), true, false);
// 		ReflectionTestUtils.setField(auction, "deletedAt", LocalDateTime.now());
// 		return auction;
// 	}
//
// 	private Auction createWonAndDeletedAuction(Long id, Product product, Long startPrice, Long currentPrice) {
// 		Auction auction = createAuction(id, product, startPrice, currentPrice,
// 			LocalDateTime.now().plusDays(1), true, true);
// 		ReflectionTestUtils.setField(auction, "deletedAt", LocalDateTime.now());
// 		return auction;
// 	}
//
// 	private Bid createBid(Long id, Auction auction, User user, Long price) {
// 		Bid bid = Bid.builder()
// 			.auction(auction)
// 			.user(user)
// 			.price(price)
// 			.build();
// 		ReflectionTestUtils.setField(bid, "id", id);
// 		return bid;
// 	}
// }
