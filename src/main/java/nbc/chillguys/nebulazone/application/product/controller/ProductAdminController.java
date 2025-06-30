package nbc.chillguys.nebulazone.application.product.controller;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.application.product.dto.request.ProductAdminSearchRequest;
import nbc.chillguys.nebulazone.application.product.dto.request.ProductAdminUpdateRequest;
import nbc.chillguys.nebulazone.application.product.dto.request.UpdateImagesProductRequest;
import nbc.chillguys.nebulazone.application.product.dto.response.ProductAdminResponse;
import nbc.chillguys.nebulazone.application.product.dto.response.ProductResponse;
import nbc.chillguys.nebulazone.application.product.service.ProductAdminService;
import nbc.chillguys.nebulazone.common.response.CommonPageResponse;
import nbc.chillguys.nebulazone.domain.common.validator.image.ImageFile;
import nbc.chillguys.nebulazone.domain.product.entity.ProductTxMethod;

@RestController
@RequestMapping("/admin/products")
@RequiredArgsConstructor
public class ProductAdminController {
	private final ProductAdminService productAdminService;

	@GetMapping
	public ResponseEntity<CommonPageResponse<ProductAdminResponse>> findProducts(
		@RequestParam(value = "keyword", required = false) String keyword,
		@RequestParam(value = "txMethod", required = false) ProductTxMethod txMethod,
		@RequestParam(value = "isSold", required = false) Boolean isSold,
		@RequestParam(value = "page", defaultValue = "1") int page,
		@RequestParam(value = "size", defaultValue = "10") int size
	) {
		ProductAdminSearchRequest request = new ProductAdminSearchRequest(keyword, txMethod, isSold, page, size);
		Pageable pageable = PageRequest.of(page - 1, size);
		CommonPageResponse<ProductAdminResponse> response = productAdminService.findProducts(
			request, pageable);
		return ResponseEntity.ok(response);
	}

	@GetMapping("/{productId}")
	public ResponseEntity<ProductAdminResponse> getProduct(@PathVariable Long productId) {
		ProductAdminResponse response = productAdminService.getProduct(productId);
		return ResponseEntity.ok(response);
	}

	@PatchMapping("/{productId}")
	public ResponseEntity<Void> updateProduct(
		@PathVariable Long productId,
		@RequestBody ProductAdminUpdateRequest request
	) {
		productAdminService.updateProduct(productId, request);
		return ResponseEntity.noContent().build();
	}

	@DeleteMapping("/{productId}")
	public ResponseEntity<Void> deleteProduct(@PathVariable Long productId) {
		productAdminService.deleteProduct(productId);
		return ResponseEntity.noContent().build();
	}

	@PostMapping("/{productId}/restore")
	public ResponseEntity<Void> restoreProduct(@PathVariable Long productId) {
		productAdminService.restoreProduct(productId);
		return ResponseEntity.noContent().build();
	}

	@PutMapping(value = "/{productId}/images",
		consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<ProductResponse> updateProductImages(
		@ImageFile @RequestPart(value = "images", required = false) List<MultipartFile> imageFiles,
		@PathVariable("productId") Long productId,
		@Valid @RequestPart("product") UpdateImagesProductRequest request
	) {

		ProductResponse response = productAdminService.updateProductImages(productId, imageFiles,
			request.remainImageUrls());

		return ResponseEntity.ok(response);
	}

}
