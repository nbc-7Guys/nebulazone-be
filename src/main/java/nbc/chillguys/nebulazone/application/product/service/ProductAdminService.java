package nbc.chillguys.nebulazone.application.product.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.application.auction.service.AuctionRedisService;
import nbc.chillguys.nebulazone.application.product.dto.request.ProductAdminSearchRequest;
import nbc.chillguys.nebulazone.application.product.dto.request.ProductAdminUpdateRequest;
import nbc.chillguys.nebulazone.application.product.dto.response.ProductAdminResponse;
import nbc.chillguys.nebulazone.application.product.dto.response.ProductResponse;
import nbc.chillguys.nebulazone.common.response.CommonPageResponse;
import nbc.chillguys.nebulazone.domain.product.dto.ProductAdminInfo;
import nbc.chillguys.nebulazone.domain.product.dto.ProductAdminSearchQueryCommand;
import nbc.chillguys.nebulazone.domain.product.entity.Product;
import nbc.chillguys.nebulazone.domain.product.entity.ProductTxMethod;
import nbc.chillguys.nebulazone.domain.product.event.ProductCreatedEvent;
import nbc.chillguys.nebulazone.domain.product.event.ProductDeletedEvent;
import nbc.chillguys.nebulazone.domain.product.event.ProductUpdatedEvent;
import nbc.chillguys.nebulazone.domain.product.service.ProductAdminDomainService;
import nbc.chillguys.nebulazone.infra.gcs.client.GcsClient;

@Service
@RequiredArgsConstructor
public class ProductAdminService {

	private final ProductAdminDomainService productAdminDomainService;
	private final AuctionRedisService auctionRedisService;
	private final GcsClient gcsClient;
	private final ApplicationEventPublisher eventPublisher;

	public CommonPageResponse<ProductAdminResponse> findProducts(ProductAdminSearchRequest request, Pageable pageable) {
		ProductAdminSearchQueryCommand command = new ProductAdminSearchQueryCommand(
			request.keyword(),
			request.txMethod(),
			request.isSold()
		);
		Page<ProductAdminInfo> infoPage = productAdminDomainService.findProducts(command, pageable);
		return CommonPageResponse.from(infoPage.map(ProductAdminResponse::from));
	}

	public ProductAdminResponse getProduct(Long productId) {
		ProductAdminInfo info = productAdminDomainService.getProduct(productId);
		return ProductAdminResponse.from(info);
	}

	public void updateProduct(Long productId, ProductAdminUpdateRequest request) {
		Product product = productAdminDomainService.updateProduct(productId, request);
		eventPublisher.publishEvent(new ProductUpdatedEvent(product));

	}

	public void deleteProduct(Long productId) {
		Long deletedProductId = productAdminDomainService.deleteProduct(productId);
		eventPublisher.publishEvent(new ProductDeletedEvent(deletedProductId));

	}

	public void restoreProduct(Long productId) {
		Product product = productAdminDomainService.restoreProduct(productId);
		eventPublisher.publishEvent(new ProductCreatedEvent(product));

	}

	@Transactional
	public ProductResponse updateProductImages(Long productId, List<MultipartFile> imageFiles,
		List<String> remainImageUrls) {

		List<String> productImageUrs = new ArrayList<>(remainImageUrls);
		boolean hasImage = imageFiles != null && !imageFiles.isEmpty();
		if (hasImage) {
			List<String> newImageUrls = imageFiles.stream()
				.map(gcsClient::uploadFile)
				.toList();
			productImageUrs.addAll(newImageUrls);

		}

		Product product = productAdminDomainService.findByIdWithJoin(productId);

		product.getProductImages().stream()
			.filter(postImage -> !productImageUrs.contains(postImage.getUrl()))
			.forEach((postImage) -> gcsClient.deleteFile(postImage.getUrl()));

		Product updatedProduct = productAdminDomainService.updateProductImages(product, productImageUrs);

		if (updatedProduct.getTxMethod() == ProductTxMethod.AUCTION) {

			auctionRedisService.updateAuctionProductImages(updatedProduct.getAuctionId(), productImageUrs);
		}

		eventPublisher.publishEvent(new ProductUpdatedEvent(updatedProduct));

		return ProductResponse.from(updatedProduct);

	}

}
