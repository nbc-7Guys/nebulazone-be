package nbc.chillguys.nebulazone.application.products.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.application.products.dto.request.ChangeToAuctionTypeRequest;
import nbc.chillguys.nebulazone.application.products.dto.request.CreateProductRequest;
import nbc.chillguys.nebulazone.application.products.dto.request.UpdateProductRequest;
import nbc.chillguys.nebulazone.application.products.dto.response.DeleteProductResponse;
import nbc.chillguys.nebulazone.application.products.dto.response.ProductResponse;
import nbc.chillguys.nebulazone.application.products.dto.response.PurchaseProductResponse;
import nbc.chillguys.nebulazone.domain.auction.dto.AuctionCreateCommand;
import nbc.chillguys.nebulazone.domain.auction.entity.Auction;
import nbc.chillguys.nebulazone.domain.auction.service.AuctionDomainService;
import nbc.chillguys.nebulazone.domain.auth.vo.AuthUser;
import nbc.chillguys.nebulazone.domain.catalog.entity.Catalog;
import nbc.chillguys.nebulazone.domain.products.dto.ChangeToAuctionTypeCommand;
import nbc.chillguys.nebulazone.domain.products.dto.ProductCreateCommand;
import nbc.chillguys.nebulazone.domain.products.dto.ProductDeleteCommand;
import nbc.chillguys.nebulazone.domain.products.dto.ProductPurchaseCommand;
import nbc.chillguys.nebulazone.domain.products.dto.ProductUpdateCommand;
import nbc.chillguys.nebulazone.domain.products.entity.Product;
import nbc.chillguys.nebulazone.domain.products.entity.ProductTxMethod;
import nbc.chillguys.nebulazone.domain.products.service.ProductDomainService;
import nbc.chillguys.nebulazone.domain.transaction.dto.TransactionCreateCommand;
import nbc.chillguys.nebulazone.domain.transaction.entity.Transaction;
import nbc.chillguys.nebulazone.domain.transaction.service.TransactionDomainService;
import nbc.chillguys.nebulazone.domain.user.entity.User;
import nbc.chillguys.nebulazone.domain.user.service.UserDomainService;
import nbc.chillguys.nebulazone.infra.aws.s3.S3Service;

@Service
@RequiredArgsConstructor
public class ProductService {

	private final UserDomainService userDomainService;
	private final ProductDomainService productDomainService;
	private final AuctionDomainService auctionDomainService;
	private final TransactionDomainService transactionDomainService;

	// todo: private final CatalogDomainService catalogDomainService;

	private final S3Service s3Service;

	@Transactional
	public ProductResponse createProduct(AuthUser authUser, Long catalogId, CreateProductRequest request,
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
			auctionDomainService.createAuction(AuctionCreateCommand.of(createProduct, request.getProductEndTime()));
		}

		return ProductResponse.from(createProduct, request.getProductEndTime());
	}

	public ProductResponse updateProduct(
		Long userId,
		Long catalogId,
		Long productId,
		UpdateProductRequest request,
		List<MultipartFile> imageFiles
	) {
		User user = userDomainService.findActiveUserById(userId);
		Product product = productDomainService.findActiveProductById(productId);

		// todo: 카탈로그 도메인 서비스 생성 후 작업
		Catalog catalog = null;

		List<String> imageUrls = new ArrayList<>(request.remainImageUrls());
		boolean hasImage = !imageFiles.isEmpty();
		if (hasImage) {
			List<String> newImageUrls = imageFiles.stream()
				.map(s3Service::generateUploadUrlAndUploadFile)
				.toList();
			imageUrls.addAll(newImageUrls);

			product.getProductImages().stream()
				.filter(productImage -> !imageUrls.contains(productImage.getUrl()))
				.forEach((productImage) -> s3Service.generateDeleteUrlAndDeleteFile(productImage.getUrl()));
		}

		ProductUpdateCommand command = request.toCommand(user, catalog, productId, imageUrls);
		Product updatedProduct = productDomainService.updateProduct(command);

		return ProductResponse.from(updatedProduct);
	}

	@Transactional
	public ProductResponse changeToAuctionType(
		Long userId,
		Long catalogId,
		Long productId,
		ChangeToAuctionTypeRequest request
	) {
		User user = userDomainService.findActiveUserById(userId);

		// todo: 카탈로그 도메인 서비스 생성 후 작업
		Catalog catalog = null;

		ChangeToAuctionTypeCommand command = request.toCommand(user, catalog, productId);
		Product product = productDomainService.changeToAuctionType(command);

		auctionDomainService.createAuction(AuctionCreateCommand.of(product, request.getProductEndTime()));

		return ProductResponse.from(product, request.getProductEndTime());
	}

	@Transactional
	public DeleteProductResponse deleteProduct(Long userId, Long catalogId, Long productId) {
		User user = userDomainService.findActiveUserById(userId);

		// todo: 카탈로그 도메인 서비스 생성 후 작업
		Catalog catalog = null;

		Auction auction = auctionDomainService.findAuctionByProductId(productId);

		if (auction != null) {
			auction.delete();
		}

		ProductDeleteCommand command = ProductDeleteCommand.of(user, catalog, productId);
		productDomainService.deleteProduct(command);

		return DeleteProductResponse.from(productId);
	}

	@Transactional
	public PurchaseProductResponse purchaseProduct(Long userId, Long catalogId, Long productId) {
		User user = userDomainService.findActiveUserById(userId);
		Product product = productDomainService.findAvailableProductById(productId);

		// todo: 카탈로그 도메인 서비스 생성 후 작업
		Catalog catalog = null;

		user.usePoint(Math.toIntExact(product.getPrice()));

		ProductPurchaseCommand command = ProductPurchaseCommand.of(user, catalog, productId);
		productDomainService.purchaseProduct(command);

		TransactionCreateCommand txCreateCommand
			= TransactionCreateCommand.of(user, product, product.getTxMethod().name());
		Transaction tx = transactionDomainService.createTransaction(txCreateCommand);

		return PurchaseProductResponse.from(tx);
	}
}
