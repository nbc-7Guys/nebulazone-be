package nbc.chillguys.nebulazone.application.product.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.application.auction.service.AuctionRedisService;
import nbc.chillguys.nebulazone.application.product.dto.request.ChangeToAuctionTypeRequest;
import nbc.chillguys.nebulazone.application.product.dto.request.CreateProductRequest;
import nbc.chillguys.nebulazone.application.product.dto.request.UpdateProductRequest;
import nbc.chillguys.nebulazone.application.product.dto.response.DeleteProductResponse;
import nbc.chillguys.nebulazone.application.product.dto.response.ProductResponse;
import nbc.chillguys.nebulazone.application.product.dto.response.PurchaseProductResponse;
import nbc.chillguys.nebulazone.application.product.dto.response.SearchProductResponse;
import nbc.chillguys.nebulazone.domain.auction.dto.AuctionCreateCommand;
import nbc.chillguys.nebulazone.domain.auction.entity.Auction;
import nbc.chillguys.nebulazone.domain.auction.service.AuctionDomainService;
import nbc.chillguys.nebulazone.domain.catalog.entity.Catalog;
import nbc.chillguys.nebulazone.domain.catalog.service.CatalogDomainService;
import nbc.chillguys.nebulazone.domain.product.dto.ChangeToAuctionTypeCommand;
import nbc.chillguys.nebulazone.domain.product.dto.ProductCreateCommand;
import nbc.chillguys.nebulazone.domain.product.dto.ProductDeleteCommand;
import nbc.chillguys.nebulazone.domain.product.dto.ProductFindQuery;
import nbc.chillguys.nebulazone.domain.product.dto.ProductPurchaseCommand;
import nbc.chillguys.nebulazone.domain.product.dto.ProductSearchCommand;
import nbc.chillguys.nebulazone.domain.product.dto.ProductUpdateCommand;
import nbc.chillguys.nebulazone.domain.product.entity.Product;
import nbc.chillguys.nebulazone.domain.product.entity.ProductEndTime;
import nbc.chillguys.nebulazone.domain.product.entity.ProductTxMethod;
import nbc.chillguys.nebulazone.domain.product.event.PurchaseProductEvent;
import nbc.chillguys.nebulazone.domain.product.event.UpdateProductEvent;
import nbc.chillguys.nebulazone.domain.product.service.ProductDomainService;
import nbc.chillguys.nebulazone.domain.product.vo.ProductDocument;
import nbc.chillguys.nebulazone.domain.user.entity.User;
import nbc.chillguys.nebulazone.infra.gcs.client.GcsClient;
import nbc.chillguys.nebulazone.infra.redis.dto.CreateRedisAuctionDto;
import nbc.chillguys.nebulazone.infra.redis.lock.DistributedLock;

@Service
@RequiredArgsConstructor
public class ProductService {

	private final ProductDomainService productDomainService;
	private final AuctionDomainService auctionDomainService;
	private final CatalogDomainService catalogDomainService;
	private final AuctionRedisService auctionRedisService;
	private final GcsClient gcsClient;
	private final ApplicationEventPublisher eventPublisher;

	@Transactional
	public ProductResponse createProduct(User user, Long catalogId, CreateProductRequest request) {

		Catalog findCatalog = catalogDomainService.getCatalogById(catalogId);

		ProductCreateCommand productCreateCommand = ProductCreateCommand.of(user, findCatalog, request);

		ProductEndTime productEndTime = request.getProductEndTime();

		Product createdProduct = productDomainService.createProduct(productCreateCommand);

		if (createdProduct.getTxMethod() == ProductTxMethod.AUCTION) {
			AuctionCreateCommand auctionCreateCommand = AuctionCreateCommand.of(createdProduct, productEndTime);
			Auction createdAuction = auctionDomainService.createAuction(auctionCreateCommand);

			CreateRedisAuctionDto createRedisAuctionDto = CreateRedisAuctionDto.of(
				createdProduct, createdAuction, user, productEndTime);

			auctionRedisService.createAuction(createRedisAuctionDto);
			createdProduct.updateAuctionId(createdAuction.getId());
		}

		productDomainService.saveProductToEs(createdProduct);

		return ProductResponse.from(createdProduct, productEndTime);
	}

	@Transactional
	public ProductResponse updateProduct(Long productId, Long userId, Long catalogId, UpdateProductRequest request) {
		ProductUpdateCommand command = request.toCommand(productId, userId, catalogId);
		Product updatedProduct = productDomainService.updateProduct(command);

		eventPublisher.publishEvent(new UpdateProductEvent(updatedProduct));

		return ProductResponse.from(updatedProduct);
	}

	@Transactional
	public ProductResponse changeToAuctionType(
		Long productId,
		Long userId,
		Long catalogId,
		ChangeToAuctionTypeRequest request
	) {
		ChangeToAuctionTypeCommand command = request.toCommand(productId, userId, catalogId);
		Product product = productDomainService.changeToAuctionType(command);

		productDomainService.saveProductToEs(product);

		auctionDomainService.createAuction(AuctionCreateCommand.of(product, request.getProductEndTime()));

		return ProductResponse.from(product, request.getProductEndTime());
	}

	@Transactional
	public DeleteProductResponse deleteProduct(Long productId, Long userId, Long catalogId) {
		ProductDeleteCommand command = ProductDeleteCommand.of(productId, userId, catalogId);
		Product product = productDomainService.deleteProduct(command);

		productDomainService.deleteProductFromEs(productId);

		if (Objects.equals(product.getTxMethod(), ProductTxMethod.AUCTION)) {
			Auction auction = auctionDomainService.findAuctionByProductId(productId);
			auction.delete();
		}

		return DeleteProductResponse.from(productId);
	}

	@DistributedLock(key = "'lock:purchase:product:' + #productId")
	@Transactional
	public PurchaseProductResponse purchaseProduct(User user, Long catalogId, Long productId) {
		Product product = productDomainService.findAvailableProductByIdForUpdate(productId);

		product.validNotOwner(user.getId());
		product.validBelongsToCatalog(catalogId);

		user.usePoint(product.getPrice());

		ProductPurchaseCommand command = ProductPurchaseCommand.of(productId, user.getId(), catalogId);
		productDomainService.purchaseProduct(command);

		product.getSeller().addPoint(product.getPrice());

		LocalDateTime purchasedAt = LocalDateTime.now();
		eventPublisher.publishEvent(new PurchaseProductEvent(user, product, purchasedAt));

		return PurchaseProductResponse.from(product, purchasedAt);
	}

	public Page<SearchProductResponse> searchProduct(String productName, String sellerNickname,
		ProductTxMethod txMethod, Long priceFrom,
		Long priceTo, int page, int size) {
		ProductSearchCommand productSearchCommand = ProductSearchCommand.of(productName, sellerNickname, txMethod,
			priceFrom, priceTo, page, size);

		Page<ProductDocument> productDocuments = productDomainService.searchProduct(productSearchCommand);

		return productDocuments.map(SearchProductResponse::from);
	}

	public ProductResponse getProduct(Long catalogId, Long productId) {
		Catalog catalog = catalogDomainService.getCatalogById(catalogId);

		ProductFindQuery query = ProductFindQuery.of(catalog.getId(), productId);
		Product product = productDomainService.getProductByIdWithUserAndImages(query);

		return ProductResponse.from(product);
	}

	public ProductResponse updateProductImages(Long productId, List<MultipartFile> imageFiles, User user,
		List<String> remainImageUrls) {

		List<String> productImageUrs = new ArrayList<>(remainImageUrls);
		boolean hasImage = imageFiles != null && !imageFiles.isEmpty();
		if (hasImage) {
			List<String> newImageUrls = imageFiles.stream()
				.map(gcsClient::uploadFile)
				.toList();
			productImageUrs.addAll(newImageUrls);

		}

		Product product = productDomainService.findActiveProductById(productId);

		product.getProductImages().stream()
			.filter(postImage -> !productImageUrs.contains(postImage.getUrl()))
			.forEach((postImage) -> gcsClient.deleteFile(postImage.getUrl()));

		Product updatedProduct = productDomainService.updateProductImages(product, productImageUrs, user.getId());

		if (updatedProduct.getTxMethod() == ProductTxMethod.AUCTION) {

			auctionRedisService.updateAuctionProductImages(updatedProduct.getAuctionId(), productImageUrs);
		}

		productDomainService.saveProductToEs(updatedProduct);

		return ProductResponse.from(updatedProduct);
	}
}
