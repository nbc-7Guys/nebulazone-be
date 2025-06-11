package nbc.chillguys.nebulazone.application.products.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.application.product.dto.response.AdminProductResponse;
import nbc.chillguys.nebulazone.application.products.dto.request.AdminProductSearchRequest;
import nbc.chillguys.nebulazone.application.products.dto.request.AdminProductUpdateRequest;
import nbc.chillguys.nebulazone.common.response.CommonPageResponse;
import nbc.chillguys.nebulazone.domain.products.dto.AdminProductInfo;
import nbc.chillguys.nebulazone.domain.products.dto.AdminProductSearchQueryCommand;
import nbc.chillguys.nebulazone.domain.products.service.AdminProductDomainService;

@Service
@RequiredArgsConstructor
public class AdminProductService {
	private final AdminProductDomainService adminProductDomainService;

	public CommonPageResponse<AdminProductResponse> findProducts(AdminProductSearchRequest request, Pageable pageable) {
		AdminProductSearchQueryCommand command = new AdminProductSearchQueryCommand(
			request.keyword(),
			request.txMethod(),
			request.isSold()
		);
		Page<AdminProductInfo> infoPage = adminProductDomainService.findProducts(command, pageable);
		return CommonPageResponse.from(infoPage.map(AdminProductResponse::from));
	}

	public AdminProductResponse getProduct(Long productId) {
		AdminProductInfo info = adminProductDomainService.getProduct(productId);
		return AdminProductResponse.from(info);
	}

	public void updateProduct(Long productId, AdminProductUpdateRequest request) {
		adminProductDomainService.updateProduct(productId, request);
	}

	public void deleteProduct(Long productId) {
		adminProductDomainService.deleteProduct(productId);
	}

	@PostMapping("/{productId}/restore")
	public ResponseEntity<Void> restoreProduct(@PathVariable Long productId) {
		adminProductDomainService.restoreProduct(productId);
		return ResponseEntity.noContent().build();
	}

}
