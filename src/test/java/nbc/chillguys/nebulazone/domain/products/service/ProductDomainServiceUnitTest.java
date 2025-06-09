package nbc.chillguys.nebulazone.domain.products.service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
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
import org.springframework.test.util.ReflectionTestUtils;

import nbc.chillguys.nebulazone.application.products.dto.request.CreateProductRequest;
import nbc.chillguys.nebulazone.domain.catalog.entity.Catalog;
import nbc.chillguys.nebulazone.domain.products.dto.ChangeToAuctionTypeCommand;
import nbc.chillguys.nebulazone.domain.products.dto.ProductCreateCommand;
import nbc.chillguys.nebulazone.domain.products.dto.ProductDeleteCommand;
import nbc.chillguys.nebulazone.domain.products.dto.ProductPurchaseCommand;
import nbc.chillguys.nebulazone.domain.products.dto.ProductUpdateCommand;
import nbc.chillguys.nebulazone.domain.products.entity.Product;
import nbc.chillguys.nebulazone.domain.products.entity.ProductEndTime;
import nbc.chillguys.nebulazone.domain.products.entity.ProductTxMethod;
import nbc.chillguys.nebulazone.domain.products.exception.ProductErrorCode;
import nbc.chillguys.nebulazone.domain.products.exception.ProductException;
import nbc.chillguys.nebulazone.domain.products.repository.ProductRepository;
import nbc.chillguys.nebulazone.domain.user.entity.Address;
import nbc.chillguys.nebulazone.domain.user.entity.OAuthType;
import nbc.chillguys.nebulazone.domain.user.entity.User;
import nbc.chillguys.nebulazone.domain.user.entity.UserRole;

@DisplayName("판매글 도메인 서비스 단위 테스트")
@ExtendWith(MockitoExtension.class)
class ProductDomainServiceUnitTest {

	@Mock
	private ProductRepository productRepository;

	@InjectMocks
	private ProductDomainService productDomainService;

	private User user;
	private Catalog catalog;
	private Product product;
	private Product auctionProduct;

	@BeforeEach
	void init() {
		HashSet<Address> addresses = new HashSet<>();

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
	}

	@Nested
	@DisplayName("판매글 생성 테스트")
	class CreateProductTest {

		@Test
		@DisplayName("경매 판매글 등록 성공")
		void success_createProduct_auction() {
			// given
			ProductCreateCommand productCreateCommand = ProductCreateCommand.of(user, null,
				new CreateProductRequest("경매 판매글 제목1", "경매 판매글 내용1", "auction",
					2_000_000L, "hour_24"));

			List<String> imageUrls = List.of("image1.jpg, image2.jpg");

			Product savedProduct = Product.builder()
				.name("경매 판매글 제목1")
				.description("경매 판매글 내용1")
				.price(2_000_000L)
				.txMethod(ProductTxMethod.AUCTION)
				.seller(user)
				.build();
			ReflectionTestUtils.setField(savedProduct, "id", 1L);

			given(productRepository.save(any(Product.class))).will(i -> {
				Product product = i.getArgument(0);
				product.addProductImages(imageUrls);
				return product;
			});

			// when
			Product result = productDomainService.createProduct(productCreateCommand, imageUrls);

			// then
			assertThat(result.getName()).isEqualTo(productCreateCommand.name());
			assertThat(result.getDescription()).isEqualTo(productCreateCommand.description());
			assertThat(result.getPrice()).isEqualTo(productCreateCommand.price());

			verify(productRepository).save(any(Product.class));
		}

	}

	@Nested
	@DisplayName("판매 상품 수정 테스트")
	class UpdateProductTest {

		@Test
		@DisplayName("판매 상품 수정 성공")
		void success_updateProduct() {
			List<String> imageUrls = List.of("update_url.jpg");
			ProductUpdateCommand command
				= new ProductUpdateCommand(user, catalog, product.getId(), imageUrls, "수정된 이름", "수정된 본문");

			given(productRepository.findByIdAndDeletedFalse(any(Long.class))).willReturn(Optional.of(product));

			Product result = productDomainService.updateProduct(command);

			assertEquals(command.imageUrls().size(), result.getProductImages().size());
			assertEquals(command.name(), result.getName());
			assertEquals(command.description(), result.getDescription());
		}

		@Test
		@DisplayName("판매 상품 수정 실패 - 판매 상품을 찾을 수 없음")
		void fail_updateProduct_productNotFound() {
			List<String> imageUrls = List.of("update_url.jpg");
			ProductUpdateCommand command
				= new ProductUpdateCommand(user, catalog, product.getId(), imageUrls, "수정된 이름", "수정된 본문");

			given(productRepository.findByIdAndDeletedFalse(any(Long.class))).willReturn(Optional.empty());

			ProductException exception = assertThrows(ProductException.class,
				() -> productDomainService.updateProduct(command));
			assertEquals(ProductErrorCode.PRODUCT_NOT_FOUND, exception.getErrorCode());
		}

		@Test
		@DisplayName("판매 상품 수정 실패 - 카테고리에 해당 판매 상품을 찾을 수 없음")
		void fail_updateProduct_notBelongsToCatalog() {
			List<String> imageUrls = List.of("update_url.jpg");
			Catalog catalog = Catalog.builder().build();
			ReflectionTestUtils.setField(catalog, "id", 2L);

			ProductUpdateCommand command
				= new ProductUpdateCommand(user, catalog, product.getId(), imageUrls, "수정된 이름", "수정된 본문");

			given(productRepository.findByIdAndDeletedFalse(any(Long.class))).willReturn(Optional.of(product));

			ProductException exception = assertThrows(ProductException.class,
				() -> productDomainService.updateProduct(command));
			assertEquals(ProductErrorCode.NOT_BELONGS_TO_CATALOG, exception.getErrorCode());
		}

		@Test
		@DisplayName("판매 상품 수정 실패 - 판매 상품 주인이 아님")
		void fail_updateProduct_notProductOwner() {
			List<String> imageUrls = List.of("update_url.jpg");
			User user = User.builder().build();
			ReflectionTestUtils.setField(user, "id", 2L);

			ProductUpdateCommand command
				= new ProductUpdateCommand(user, catalog, product.getId(), imageUrls, "수정된 이름", "수정된 본문");

			given(productRepository.findByIdAndDeletedFalse(any(Long.class))).willReturn(Optional.of(product));

			ProductException exception = assertThrows(ProductException.class,
				() -> productDomainService.updateProduct(command));
			assertEquals(ProductErrorCode.NOT_PRODUCT_OWNER, exception.getErrorCode());
		}
	}

	@Nested
	@DisplayName("판매 방식 수정 테스트")
	class ChangeToAuctionTypeTest {

		@Test
		@DisplayName("판매 방식 수정 성공")
		void success_changeToAuctionType() {
			ChangeToAuctionTypeCommand command
				= new ChangeToAuctionTypeCommand(user, catalog, product.getId(), 100000L, ProductEndTime.HOUR_24);

			given(productRepository.findByIdAndDeletedFalse(any(Long.class))).willReturn(Optional.of(product));

			Product result = productDomainService.changeToAuctionType(command);

			assertEquals(ProductTxMethod.AUCTION, result.getTxMethod());
			assertEquals(command.price(), result.getPrice());
		}

		@Test
		@DisplayName("판매 방식 수정 실패 - 판매 상품을 찾을 수 없음")
		void fail_changeToAuctionType_productNotFound() {
			ChangeToAuctionTypeCommand command
				= new ChangeToAuctionTypeCommand(user, catalog, product.getId(), 100000L, ProductEndTime.HOUR_24);

			given(productRepository.findByIdAndDeletedFalse(any(Long.class))).willReturn(Optional.empty());

			ProductException exception = assertThrows(ProductException.class,
				() -> productDomainService.changeToAuctionType(command));
			assertEquals(ProductErrorCode.PRODUCT_NOT_FOUND, exception.getErrorCode());
		}

		@Test
		@DisplayName("판매 방식 수정 실패 - 카테고리에 해당 판매 상품을 찾을 수 없음")
		void fail_changeToAuctionType_notBelongsToCatalog() {
			Catalog catalog = Catalog.builder().build();
			ReflectionTestUtils.setField(catalog, "id", 2L);

			ChangeToAuctionTypeCommand command
				= new ChangeToAuctionTypeCommand(user, catalog, product.getId(), 100000L, ProductEndTime.HOUR_24);

			given(productRepository.findByIdAndDeletedFalse(any(Long.class))).willReturn(Optional.of(product));

			ProductException exception = assertThrows(ProductException.class,
				() -> productDomainService.changeToAuctionType(command));
			assertEquals(ProductErrorCode.NOT_BELONGS_TO_CATALOG, exception.getErrorCode());
		}

		@Test
		@DisplayName("판매 방식 수정 실패 - 판매 상품 주인이 아님")
		void fail_changeToAuctionType_notProductOwner() {
			User user = User.builder().build();
			ReflectionTestUtils.setField(user, "id", 2L);

			ChangeToAuctionTypeCommand command
				= new ChangeToAuctionTypeCommand(user, catalog, product.getId(), 100000L, ProductEndTime.HOUR_24);

			given(productRepository.findByIdAndDeletedFalse(any(Long.class))).willReturn(Optional.of(product));

			ProductException exception = assertThrows(ProductException.class,
				() -> productDomainService.changeToAuctionType(command));
			assertEquals(ProductErrorCode.NOT_PRODUCT_OWNER, exception.getErrorCode());
		}

		@Test
		@DisplayName("판매 방식 수정 실패 - 이미 판매 방식이 경매임")
		void fail_changeToAuctionType_alreadyAuctionType() {
			ChangeToAuctionTypeCommand command = new ChangeToAuctionTypeCommand(
				user, catalog, auctionProduct.getId(), 100000L, ProductEndTime.HOUR_24
			);

			given(productRepository.findByIdAndDeletedFalse(any(Long.class))).willReturn(Optional.of(auctionProduct));

			ProductException exception = assertThrows(ProductException.class,
				() -> productDomainService.changeToAuctionType(command));
			assertEquals(ProductErrorCode.ALREADY_AUCTION_TYPE, exception.getErrorCode());
		}
	}

	@Nested
	@DisplayName("판매 상품 삭제 테스트")
	class DeleteProductTest {

		@Test
		@DisplayName("판매 상품 삭제 성공")
		void success_deleteProduct() {
			ProductDeleteCommand command = new ProductDeleteCommand(user, catalog, product.getId());

			given(productRepository.findByIdAndDeletedFalse(any(Long.class))).willReturn(Optional.of(product));

			productDomainService.deleteProduct(command);

			assertTrue(product.isDeleted());
			assertNotNull(product.getDeletedAt());
		}

		@Test
		@DisplayName("판매 상품 삭제 실패 - 판매 상품을 찾을 수 없음")
		void fail_changeToAuctionType_productNotFound() {
			ProductDeleteCommand command = new ProductDeleteCommand(user, catalog, product.getId());


			given(productRepository.findByIdAndDeletedFalse(any(Long.class))).willReturn(Optional.empty());

			ProductException exception = assertThrows(ProductException.class,
				() -> productDomainService.deleteProduct(command));
			assertEquals(ProductErrorCode.PRODUCT_NOT_FOUND, exception.getErrorCode());
		}

		@Test
		@DisplayName("판매 상품 삭제 실패 - 카테고리에 해당 판매 상품을 찾을 수 없음")
		void fail_changeToAuctionType_notBelongsToCatalog() {
			Catalog catalog = Catalog.builder().build();
			ReflectionTestUtils.setField(catalog, "id", 2L);

			ProductDeleteCommand command = new ProductDeleteCommand(user, catalog, product.getId());

			given(productRepository.findByIdAndDeletedFalse(any(Long.class))).willReturn(Optional.of(product));

			ProductException exception = assertThrows(ProductException.class,
				() -> productDomainService.deleteProduct(command));
			assertEquals(ProductErrorCode.NOT_BELONGS_TO_CATALOG, exception.getErrorCode());
		}

		@Test
		@DisplayName("판매 상품 삭제 실패 - 판매 상품 주인이 아님")
		void fail_changeToAuctionType_notProductOwner() {
			User user = User.builder().build();
			ReflectionTestUtils.setField(user, "id", 2L);

			ProductDeleteCommand command = new ProductDeleteCommand(user, catalog, product.getId());

			given(productRepository.findByIdAndDeletedFalse(any(Long.class))).willReturn(Optional.of(product));

			ProductException exception = assertThrows(ProductException.class,
				() -> productDomainService.deleteProduct(command));
			assertEquals(ProductErrorCode.NOT_PRODUCT_OWNER, exception.getErrorCode());
		}
	}

	@Nested
	@DisplayName("판매 상품 구매 테스트")
	class PurchaseProductTest {

		@Test
		@DisplayName("판매 상품 구매 성공")
		void success_purchaseProduct() {
			ProductPurchaseCommand command = new ProductPurchaseCommand(user, catalog, product.getId());

			given(productRepository.findByIdAndDeletedFalse(any(Long.class))).willReturn(Optional.of(product));

			productDomainService.purchaseProduct(command);

			assertTrue(product.isSold());
		}

		@Test
		@DisplayName("판매 상품 구매 실패 - 판매 상품을 찾을 수 없음")
		void fail_purchaseProduct_productNotFound() {
			ProductPurchaseCommand command = new ProductPurchaseCommand(user, catalog, product.getId());

			given(productRepository.findByIdAndDeletedFalse(any(Long.class))).willReturn(Optional.empty());

			ProductException exception = assertThrows(ProductException.class,
				() -> productDomainService.purchaseProduct(command));
			assertEquals(ProductErrorCode.PRODUCT_NOT_FOUND, exception.getErrorCode());
		}

		@Test
		@DisplayName("판매 상품 구매 실패 - 판매 상품을 찾을 수 없음")
		void fail_purchaseProduct_alreadySold() {
			success_purchaseProduct();

			ProductPurchaseCommand command = new ProductPurchaseCommand(user, catalog, product.getId());

			given(productRepository.findByIdAndDeletedFalse(any(Long.class))).willReturn(Optional.of(product));

			ProductException exception = assertThrows(ProductException.class,
				() -> productDomainService.purchaseProduct(command));
			assertEquals(ProductErrorCode.ALREADY_SOLD, exception.getErrorCode());
		}

		@Test
		@DisplayName("판매 상품 구매 실패 - 옥션 상품은 구매 불가")
		void fail_purchaseProduct_auctionProductNotPurchasable() {
			ProductPurchaseCommand command = new ProductPurchaseCommand(user, catalog, auctionProduct.getId());

			given(productRepository.findByIdAndDeletedFalse(any(Long.class))).willReturn(Optional.of(auctionProduct));

			ProductException exception = assertThrows(ProductException.class,
				() -> productDomainService.purchaseProduct(command));
			assertEquals(ProductErrorCode.AUCTION_PRODUCT_NOT_PURCHASABLE, exception.getErrorCode());
		}
	}
}
