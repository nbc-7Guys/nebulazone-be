package nbc.chillguys.nebulazone.application.products.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.application.products.dto.request.CreateProductRequest;
import nbc.chillguys.nebulazone.application.products.dto.request.UpdateProductRequest;
import nbc.chillguys.nebulazone.application.products.dto.response.DeleteProductResponse;
import nbc.chillguys.nebulazone.application.products.dto.response.ProductResponse;
import nbc.chillguys.nebulazone.domain.auth.vo.AuthUser;
import nbc.chillguys.nebulazone.domain.catalog.entity.Catalog;
import nbc.chillguys.nebulazone.domain.products.dto.ProductCreateCommand;
import nbc.chillguys.nebulazone.domain.products.dto.ProductDeleteCommand;
import nbc.chillguys.nebulazone.domain.products.dto.ProductUpdateCommand;
import nbc.chillguys.nebulazone.domain.products.entity.Product;
import nbc.chillguys.nebulazone.domain.products.entity.ProductTxMethod;
import nbc.chillguys.nebulazone.domain.products.service.ProductDomainService;
import nbc.chillguys.nebulazone.domain.user.entity.User;
import nbc.chillguys.nebulazone.domain.user.service.UserDomainService;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

	private final UserDomainService userDomainService;
	private final ProductDomainService productDomainService;
	// todo: private final CatalogDomainService catalogDomainService;
	// todo: private final AuctionDomainService auctionDomainService;

	// todo: private final S3Service s3Service;

	@Transactional
	public ProductResponse createProduct(AuthUser authUser, Long catalogId, CreateProductRequest request,
		List<MultipartFile> multipartFiles) {

		User findUser = userDomainService.findActiveUserById(authUser.getId());

		// todo: controller에서 넘어온 이미지들을 url 리스트로 변환한 다음 Product를 생성
		// List<String> productImageUrls = s3Service.createImageUrls(multipartFiles);

		// todo: 카탈로그 도메인 서비스 생성되면 추후 붙일 예정
		// Catalog findCatalog = catalogDomainService.getCatalogById(catalogId);

		ProductCreateCommand productCreateCommand = ProductCreateCommand.of(findUser, null, request);

		Product createProduct = productDomainService.createProduct(productCreateCommand, new ArrayList<>());

		if (createProduct.getTxMethod() == ProductTxMethod.AUCTION) {
			// todo: 메서드 타입이 옥션이면 자동으로 옥션 생성할 예정.....
			// auctionDomainService.createProduct(...);
		}

		return ProductResponse.from(createProduct);
	}

	public ProductResponse updateProduct(
		Long userId,
		Long catalogId,
		Long productId,
		UpdateProductRequest request
	) {
		User user = userDomainService.findActiveUserById(userId);

		// todo: 카탈로그 도메인 서비스 생성 후 작업
		Catalog catalog = null;

		ProductUpdateCommand command = request.toCommand(user, catalog, productId);
		Product product = productDomainService.updateProduct(command);

		return ProductResponse.from(product);
	}

	public DeleteProductResponse deleteProduct(Long userId, Long catalogId, Long productId) {
		User user = userDomainService.findActiveUserById(userId);

		// todo: 카탈로그 도메인 서비스 생성 후 작업
		Catalog catalog = null;

		ProductDeleteCommand command = ProductDeleteCommand.of(user, catalog, productId);
		productDomainService.deleteProduct(command);

		return DeleteProductResponse.from(productId);
	}
}
