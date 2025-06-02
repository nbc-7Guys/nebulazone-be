package nbc.chillguys.nebulazone.domain.products.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import nbc.chillguys.nebulazone.application.products.dto.request.CreateProductRequest;
import nbc.chillguys.nebulazone.domain.products.dto.ProductCreateCommand;
import nbc.chillguys.nebulazone.domain.products.entity.Product;
import nbc.chillguys.nebulazone.domain.products.entity.ProductTxMethod;
import nbc.chillguys.nebulazone.domain.products.repository.ProductRepository;
import nbc.chillguys.nebulazone.domain.user.entity.Address;
import nbc.chillguys.nebulazone.domain.user.entity.OAuthType;
import nbc.chillguys.nebulazone.domain.user.entity.User;
import nbc.chillguys.nebulazone.domain.user.entity.UserRole;

@ExtendWith(MockitoExtension.class)
class ProductDomainServiceUnitTest {

	@Mock
	private ProductRepository productRepository;

	@InjectMocks
	private ProductDomainService productDomainService;

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
			.oauthType(OAuthType.DOMAIN)
			.roles(Set.of(UserRole.ROLE_USER))
			.addresses(addresses)
			.build();

		ReflectionTestUtils.setField(user, "id", 1L);
	}

	private User user;

	@Test
	void create_product_auction_success() {
		// given
		ProductCreateCommand productCreateCommand = ProductCreateCommand.of(user, null,
			new CreateProductRequest("경매 판매글 제목1", "경매 판매글 내용1", "auction",
				2_000_000L, "hour_24"));

		List<String> imageUrls = List.of("image1.jpg, image2.jpg");

		Product savedProduct = Product.of(
			"경매 판매글 제목1",
			"경매 판매글 내용1",
			2_000_000L,
			ProductTxMethod.AUCTION,
			user,
			null);
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
