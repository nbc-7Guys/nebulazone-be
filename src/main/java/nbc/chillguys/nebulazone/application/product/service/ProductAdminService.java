package nbc.chillguys.nebulazone.application.product.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.application.product.dto.request.ProductAdminSearchRequest;
import nbc.chillguys.nebulazone.application.product.dto.request.ProductAdminUpdateRequest;
import nbc.chillguys.nebulazone.application.product.dto.response.ProductAdminResponse;
import nbc.chillguys.nebulazone.common.response.CommonPageResponse;
import nbc.chillguys.nebulazone.domain.product.dto.ProductAdminInfo;
import nbc.chillguys.nebulazone.domain.product.dto.ProductAdminSearchQueryCommand;
import nbc.chillguys.nebulazone.domain.product.service.ProductAdminDomainService;

@Service
@RequiredArgsConstructor
public class ProductAdminService {
	private final ProductAdminDomainService productAdminDomainService;

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
		productAdminDomainService.updateProduct(productId, request);
	}

	public void deleteProduct(Long productId) {
		productAdminDomainService.deleteProduct(productId);
	}

	@PostMapping("/{productId}/restore")
	public ResponseEntity<Void> restoreProduct(@PathVariable Long productId) {
		productAdminDomainService.restoreProduct(productId);
		return ResponseEntity.noContent().build();
	}

}
