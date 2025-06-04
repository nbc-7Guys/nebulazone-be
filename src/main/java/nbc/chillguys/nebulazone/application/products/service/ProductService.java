package nbc.chillguys.nebulazone.application.products.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.application.products.dto.request.CreateProductRequest;
import nbc.chillguys.nebulazone.application.products.dto.response.CreateProductResponse;
import nbc.chillguys.nebulazone.domain.auction.dto.AuctionCreateCommand;
import nbc.chillguys.nebulazone.domain.auction.service.AuctionDomainService;
import nbc.chillguys.nebulazone.domain.auth.vo.AuthUser;
import nbc.chillguys.nebulazone.domain.products.dto.ProductCreateCommand;
import nbc.chillguys.nebulazone.domain.products.entity.Product;
import nbc.chillguys.nebulazone.domain.products.entity.ProductTxMethod;
import nbc.chillguys.nebulazone.domain.products.service.ProductDomainService;
import nbc.chillguys.nebulazone.domain.user.entity.User;
import nbc.chillguys.nebulazone.domain.user.service.UserDomainService;
import nbc.chillguys.nebulazone.infra.aws.s3.S3Service;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

	private final UserDomainService userDomainService;
	private final ProductDomainService productDomainService;
	// todo: private final CatalogDomainService catalogDomainService;
	private final AuctionDomainService auctionDomainService;

	private final S3Service s3Service;

	@Transactional
	public CreateProductResponse createProduct(AuthUser authUser, Long catalogId, CreateProductRequest request,
		List<MultipartFile> multipartFiles) {

		User findUser = userDomainService.findActiveUserById(authUser.getId());

		List<String> productImageUrls = multipartFiles == null
			? List.of()
			: multipartFiles.stream()
			.map(s3Service::generateUploadUrlAndUploadFile)
			.toList();

		// todo: 카탈로그 도메인 서비스 생성되면 추후 붙일 예정
		// Catalog findCatalog = catalogDomainService.getCatalogById(catalogId);

		ProductCreateCommand productCreateCommand = ProductCreateCommand.of(findUser, null, request);

		Product createProduct = productDomainService.createProduct(productCreateCommand, productImageUrls);

		if (createProduct.getTxMethod() == ProductTxMethod.AUCTION) {
			auctionDomainService.createAuction(AuctionCreateCommand.of(createProduct, productCreateCommand));
		}

		return CreateProductResponse.from(createProduct, productImageUrls);
	}
}
