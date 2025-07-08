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
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import nbc.chillguys.nebulazone.application.auction.service.AuctionRedisService;
import nbc.chillguys.nebulazone.application.product.dto.request.CreateProductRequest;
import nbc.chillguys.nebulazone.application.product.dto.response.ProductResponse;
import nbc.chillguys.nebulazone.application.product.dto.response.SearchProductResponse;
import nbc.chillguys.nebulazone.domain.auction.dto.AuctionCreateCommand;
import nbc.chillguys.nebulazone.domain.auction.entity.Auction;
import nbc.chillguys.nebulazone.domain.auction.service.AuctionDomainService;
import nbc.chillguys.nebulazone.domain.catalog.entity.Catalog;
import nbc.chillguys.nebulazone.domain.catalog.service.CatalogDomainService;
import nbc.chillguys.nebulazone.domain.product.dto.ProductCreateCommand;
import nbc.chillguys.nebulazone.domain.product.dto.ProductFindQuery;
import nbc.chillguys.nebulazone.domain.product.dto.ProductSearchCommand;
import nbc.chillguys.nebulazone.domain.product.entity.Product;
import nbc.chillguys.nebulazone.domain.product.entity.ProductEndTime;
import nbc.chillguys.nebulazone.domain.product.entity.ProductTxMethod;
import nbc.chillguys.nebulazone.domain.product.event.ProductCreatedEvent;
import nbc.chillguys.nebulazone.domain.product.event.ProductUpdatedEvent;
import nbc.chillguys.nebulazone.domain.product.service.ProductDomainService;
import nbc.chillguys.nebulazone.domain.product.vo.ProductDocument;
import nbc.chillguys.nebulazone.domain.user.entity.Address;
import nbc.chillguys.nebulazone.domain.user.entity.OAuthType;
import nbc.chillguys.nebulazone.domain.user.entity.User;
import nbc.chillguys.nebulazone.domain.user.entity.UserRole;
import nbc.chillguys.nebulazone.infra.gcs.client.GcsClient;
import nbc.chillguys.nebulazone.infra.redis.dto.CreateRedisAuctionDto;

@DisplayName("상품 애플리케이션 서비스 단위 테스트")
@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

	@Mock
	private ProductDomainService productDomainService;

	@Mock
	private AuctionDomainService auctionDomainService;

	@Mock
	private CatalogDomainService catalogDomainService;

	@Mock
	private AuctionRedisService auctionRedisService;

	@Mock
	private GcsClient gcsClient;

	@Mock
	private ApplicationEventPublisher eventPublisher;

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
			.startPrice(2_000_000L)
			.currentPrice(0L)
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

	@Nested
	@DisplayName("상품 생성 테스트")
	class CreateProductTest {

		@DisplayName("상품 생성 성공 - 경매")
		@Test
		void success_createProduct_auctionProduct() {
			// given
			Long catalogId = 1L;
			CreateProductRequest request = new CreateProductRequest(
				"경매 판매글 제목1",
				"경매 판매글 내용1",
				"auction",
				2_000_000L,
				"hour_12");

			given(catalogDomainService.getCatalogById(catalogId)).willReturn(catalog);
			given(productDomainService.createProduct(any(ProductCreateCommand.class))).willReturn(auctionProduct);
			given(auctionDomainService.createAuction(any(AuctionCreateCommand.class))).willReturn(auction);
			willDoNothing().given(auctionRedisService).createAuction(any(CreateRedisAuctionDto.class));
			willDoNothing().given(eventPublisher).publishEvent(any(ProductCreatedEvent.class));

			// when
			ProductResponse result = productService.createProduct(user, catalogId, request);

			// then

			verify(catalogDomainService, times(1)).getCatalogById(catalogId);
			verify(productDomainService, times(1)).createProduct(any(ProductCreateCommand.class));
			verify(auctionDomainService, times(1)).createAuction(any(AuctionCreateCommand.class));

			assertThat(result.productName()).isEqualTo(auctionProduct.getName());
			assertThat(result.productPrice()).isEqualTo(auctionProduct.getPrice());
			assertThat(result.productTxMethod()).isEqualTo(auctionProduct.getTxMethod());
			assertThat(result.endTime()).isEqualTo(ProductEndTime.from(request.endTime()));
		}

		@DisplayName("상품 생성 성공 - 즉시구매")
		@Test
		void success_createProduct_direct() {
			// given
			Long catalogId = 1L;
			CreateProductRequest request = new CreateProductRequest(
				"일반 판매글 제목1",
				"일반 판매글 내용1",
				"direct",
				2_000_000L,
				null);

			given(catalogDomainService.getCatalogById(catalogId)).willReturn(catalog);
			given(productDomainService.createProduct(any(ProductCreateCommand.class))).willReturn(product);
			willDoNothing().given(eventPublisher).publishEvent(any(ProductCreatedEvent.class));

			// when
			ProductResponse result = productService.createProduct(user, catalogId, request);

			// then
			verify(catalogDomainService, times(1)).getCatalogById(catalogId);
			verify(productDomainService, times(1)).createProduct(any(ProductCreateCommand.class));

			assertThat(result.productName()).isEqualTo(product.getName());
			assertThat(result.productPrice()).isEqualTo(product.getPrice());
			assertThat(result.productTxMethod()).isEqualTo(product.getTxMethod());
			assertThat(result.endTime()).isNull();
		}
	}

	@Nested
	@DisplayName("상품 이미지 수정 테스트")
	class UpdateProductImagesTest {

		@DisplayName("상품 이미지 수정 성공")
		@Test
		void success_updateProductImages() {
			// given

			Long updateProductId = 1L;
			product.updateProductImage(List.of("old_image_url1", "old_image_url2"));

			List<String> remainImageUrls = List.of("old_image_url1");

			List<MultipartFile> newImageFiles = List.of(
				new MockMultipartFile(
					"new_image1",
					"new_image1.jpg",
					"image/jpeg",
					"new_image1_content"
						.getBytes()),

				new MockMultipartFile(
					"new_image2",
					"new_image2.jpg",
					"image/jpeg",
					"new_image2_content"
						.getBytes()));

			given(gcsClient.uploadFile(any(MultipartFile.class)))
				.willReturn("new_image_url1", "new_image_url2");

			List<String> updatedImageUrls = List.of(
				"old_image_url1",
				"new_image_url1",
				"new_image_url2"
			);
			product.updateProductImage(updatedImageUrls);

			given(productDomainService.findActiveProductById(updateProductId)).willReturn(product);
			given(productDomainService.updateProductImages(any(Product.class), eq(updatedImageUrls), eq(user.getId())))
				.willReturn(product);
			willDoNothing().given(eventPublisher).publishEvent(any(ProductUpdatedEvent.class));

			// when
			ProductResponse result = productService.updateProductImages(updateProductId, newImageFiles, user,
				remainImageUrls);

			// then
			verify(gcsClient, times(2)).uploadFile(any(MultipartFile.class));
			verify(productDomainService, times(1)).findActiveProductById(updateProductId);
			verify(productDomainService, times(1))
				.updateProductImages(any(Product.class), anyList(), anyLong());

			assertThat(result.productImageUrls())
				.containsExactly("old_image_url1", "new_image_url1", "new_image_url2");
			assertThat(result.productImageUrls()).doesNotContain("old_image_url2");
		}

	}
}
