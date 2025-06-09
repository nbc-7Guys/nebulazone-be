package nbc.chillguys.nebulazone.application.products.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.application.products.dto.request.ChangeToAuctionTypeRequest;
import nbc.chillguys.nebulazone.application.products.dto.request.CreateProductRequest;
import nbc.chillguys.nebulazone.application.products.dto.request.UpdateProductRequest;
import nbc.chillguys.nebulazone.application.products.dto.response.DeleteProductResponse;
import nbc.chillguys.nebulazone.application.products.dto.response.ProductResponse;
import nbc.chillguys.nebulazone.application.products.dto.response.PurchaseProductResponse;
import nbc.chillguys.nebulazone.application.products.service.ProductService;
import nbc.chillguys.nebulazone.domain.auth.vo.AuthUser;
import nbc.chillguys.nebulazone.domain.common.validator.image.ImageFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/catalogs/{catalogId}/products")
public class ProductController {

	private final ProductService productService;

	@PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<ProductResponse> createProduct(
		@AuthenticationPrincipal AuthUser authUser,
		@PathVariable("catalogId") Long catalogId,
		@Valid @RequestPart("product") CreateProductRequest request,
		@RequestPart(value = "images", required = false) List<MultipartFile> multipartFiles) {

		ProductResponse productResponse = productService.createProduct(authUser, catalogId, request,
			multipartFiles);

		return ResponseEntity.status(HttpStatus.CREATED).body(productResponse);
	}

	@PutMapping("/{productId}")
	public ResponseEntity<ProductResponse> updateProduct(
		@AuthenticationPrincipal AuthUser authUser,
		@PathVariable("catalogId") Long catalogId,
		@PathVariable("productId") Long productId,
		@Valid @RequestPart("product") UpdateProductRequest request,
		@ImageFile @RequestPart(value = "images", required = false) List<MultipartFile> imageFiles
	) {
		ProductResponse response
			= productService.updateProduct(authUser.getId(), catalogId, productId, request, imageFiles);

		return ResponseEntity.ok(response);
	}

	@PatchMapping("/{productId}/auction-type")
	public ResponseEntity<ProductResponse> changeToAuctionType(
		@AuthenticationPrincipal AuthUser authUser,
		@PathVariable("catalogId") Long catalogId,
		@PathVariable("productId") Long productId,
		@Valid @RequestBody ChangeToAuctionTypeRequest request
	) {
		ProductResponse response = productService.changeToAuctionType(authUser.getId(), catalogId, productId, request);

		return ResponseEntity.ok(response);
	}

	@DeleteMapping("/{productId}")
	public ResponseEntity<DeleteProductResponse> deleteProduct(
		@AuthenticationPrincipal AuthUser authUser,
		@PathVariable("catalogId") Long catalogId,
		@PathVariable("productId") Long productId
	) {
		DeleteProductResponse response = productService.deleteProduct(authUser.getId(), catalogId, productId);

		return ResponseEntity.ok(response);
	}

	@PostMapping("/{productId}/purchase")
	public ResponseEntity<PurchaseProductResponse> purchaseProduct(
		@AuthenticationPrincipal AuthUser authUser,
		@PathVariable("catalogId") Long catalogId,
		@PathVariable("productId") Long productId
	) {
		PurchaseProductResponse response = productService.purchaseProduct(authUser.getId(), catalogId, productId);

		return ResponseEntity.ok(response);
	}
}
