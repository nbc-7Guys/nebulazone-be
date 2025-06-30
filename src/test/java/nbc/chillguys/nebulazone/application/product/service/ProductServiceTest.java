package nbc.chillguys.nebulazone.application.product.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

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
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

import nbc.chillguys.nebulazone.application.product.dto.response.ProductResponse;
import nbc.chillguys.nebulazone.application.product.dto.response.SearchProductResponse;
import nbc.chillguys.nebulazone.domain.auction.entity.Auction;
import nbc.chillguys.nebulazone.domain.auction.service.AuctionDomainService;
import nbc.chillguys.nebulazone.domain.catalog.entity.Catalog;
import nbc.chillguys.nebulazone.domain.catalog.service.CatalogDomainService;
import nbc.chillguys.nebulazone.domain.product.dto.ProductFindQuery;
import nbc.chillguys.nebulazone.domain.product.dto.ProductSearchCommand;
import nbc.chillguys.nebulazone.domain.product.entity.Product;
import nbc.chillguys.nebulazone.domain.product.entity.ProductTxMethod;
import nbc.chillguys.nebulazone.domain.product.service.ProductDomainService;
import nbc.chillguys.nebulazone.domain.product.vo.ProductDocument;
import nbc.chillguys.nebulazone.domain.transaction.service.TransactionDomainService;
import nbc.chillguys.nebulazone.domain.user.entity.Address;
import nbc.chillguys.nebulazone.domain.user.entity.OAuthType;
import nbc.chillguys.nebulazone.domain.user.entity.User;
import nbc.chillguys.nebulazone.domain.user.entity.UserRole;
import nbc.chillguys.nebulazone.infra.gcs.client.GcsClient;

@DisplayName("상품 애플리케이션 서비스 단위 테스트")
@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

	@Mock
	private ProductDomainService productDomainService;

	@Mock
	private AuctionDomainService auctionDomainService;

	@Mock
	private TransactionDomainService transactionDomainService;

	@Mock
	private CatalogDomainService catalogDomainService;

	@Mock
	private GcsClient gcsClient;

	@InjectMocks
	private ProductService productService;

	private User user;
	private Catalog catalog;
	private Product product;
	private Product auctionProduct;
	private Auction auction;

	@BeforeEach
	void init() {
		List<Address> addresses = new ArrayList<>();

		IntStream.range(1, 4)
			.forEach(i -> addresses.add(
				Address.builder()
					.addressNickname("테스트 주소 닉네임" + i)
					.roadAddress("도로명 주소 테스트" + i)
					.detailAddress("상세 주소 테스트" + i)
					.build()
			));

		user = User.builder()
			.email("test@test.com")
			.password("password")
			.phone("01012345678")
			.nickname("테스트닉")
			.profileImage("test.jpg")
			.point(0)
			.oAuthType(OAuthType.DOMAIN)
			.roles(Set.of(UserRole.ROLE_USER))
			.addresses(addresses)
			.build();

		ReflectionTestUtils.setField(user, "id", 1L);

		catalog = Catalog.builder()
			.build();

		ReflectionTestUtils.setField(catalog, "id", 1L);

		product = Product.builder()
			.name("일반 판매글 제목1")
			.description("일반 판매글 내용1")
			.price(2_000_000L)
			.txMethod(ProductTxMethod.DIRECT)
			.seller(user)
			.catalog(catalog)
			.build();

		ReflectionTestUtils.setField(product, "id", 1L);

		auctionProduct = Product.builder()
			.name("경매 판매글 제목1")
			.description("경매 판매글 내용1")
			.price(2_000_000L)
			.txMethod(ProductTxMethod.AUCTION)
			.seller(user)
			.catalog(catalog)
			.build();

		ReflectionTestUtils.setField(auctionProduct, "id", 2L);

		auction = Auction.builder()
			.product(auctionProduct)
			.startPrice(30000L)
			.currentPrice(30000L)
			.endTime(LocalDateTime.now().plusHours(12))
			.isWon(false)
			.build();

		ReflectionTestUtils.setField(auction, "id", 1L);
	}

	@Nested
	@DisplayName("상품 검색 테스트")
	class SearchProductTest {
		@Test
		@DisplayName("상품 검색 성공")
		void success_searchProduct() {
			// Given
			ProductSearchCommand command = ProductSearchCommand.of("테스트", "test", ProductTxMethod.DIRECT,
				0L, 10000L, 1, 10);

			given(productDomainService.searchProduct(command))
				.willReturn(new PageImpl<>(List.of(ProductDocument.from(product)),
					PageRequest.of(0, 10), 1L));

			// When
			Page<SearchProductResponse> responses = productService.searchProduct("테스트", "test",
				ProductTxMethod.DIRECT, 0L, 10000L, 1, 10);

			// Then
			assertThat(responses.getContent()).hasSize(1);

			verify(productDomainService, times(1)).searchProduct(command);

		}
	}

	@Nested
	@DisplayName("상품 조회 테스트")
	class GetProductTest {
		@Test
		@DisplayName("상품 조회 성공")
		void success_getProduct() {
			// Given
			Long productId = 1L;
			Long catalogId = 1L;
			ProductFindQuery query = new ProductFindQuery(catalogId, productId);

			given(catalogDomainService.getCatalogById(catalogId))
				.willReturn(catalog);
			given(productDomainService.getProductByIdWithUserAndImages(query))
				.willReturn(product);

			// When
			ProductResponse response = productService.getProduct(catalogId, productId);

			// Then
			assertThat(response.productId()).isEqualTo(productId);
			assertThat(response.productName()).isEqualTo(product.getName());
			assertThat(response.productDescription()).isEqualTo(product.getDescription());
			assertThat(response.productPrice()).isEqualTo(product.getPrice());

			verify(productDomainService, times(1)).getProductByIdWithUserAndImages(query);

		}
	}

	// @Nested
	// @DisplayName("상품 생성 테스트")
	// class CreateProductTest {
	// 	// 작성 해야함
	// }

}
