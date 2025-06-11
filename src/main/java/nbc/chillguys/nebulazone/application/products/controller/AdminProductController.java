package nbc.chillguys.nebulazone.application.products.controller;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.application.product.dto.response.AdminProductResponse;
import nbc.chillguys.nebulazone.application.products.dto.request.AdminProductSearchRequest;
import nbc.chillguys.nebulazone.application.products.dto.request.AdminProductUpdateRequest;
import nbc.chillguys.nebulazone.application.products.service.AdminProductService;
import nbc.chillguys.nebulazone.common.response.CommonPageResponse;
import nbc.chillguys.nebulazone.domain.products.entity.ProductTxMethod;

@RestController
@RequestMapping("/admin/products")
@RequiredArgsConstructor
public class AdminProductController {
	private final AdminProductService adminProductService;

	@GetMapping
	public ResponseEntity<CommonPageResponse<AdminProductResponse>> findProducts(
		@RequestParam(value = "keyword", required = false) String keyword,
		@RequestParam(value = "txMethod", required = false) ProductTxMethod txMethod,
		@RequestParam(value = "isSold", required = false) Boolean isSold,
		@RequestParam(value = "page", defaultValue = "1") int page,
		@RequestParam(value = "size", defaultValue = "10") int size
	) {
		AdminProductSearchRequest request = new AdminProductSearchRequest(keyword, txMethod, isSold, page, size);
		Pageable pageable = PageRequest.of(page - 1, size);
		CommonPageResponse<AdminProductResponse> response = adminProductService.findProducts(
			request, pageable);
		return ResponseEntity.ok(response);
	}

	@GetMapping("/{productId}")
	public ResponseEntity<AdminProductResponse> getProduct(@PathVariable Long productId) {
		AdminProductResponse response = adminProductService.getProduct(productId);
		return ResponseEntity.ok(response);
	}

	@PatchMapping("/{productId}")
	public ResponseEntity<Void> updateProduct(
		@PathVariable Long productId,
		@RequestBody AdminProductUpdateRequest request
	) {
		adminProductService.updateProduct(productId, request);
		return ResponseEntity.noContent().build();
	}

}
