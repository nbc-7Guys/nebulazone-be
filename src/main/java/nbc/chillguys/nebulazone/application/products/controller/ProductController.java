package nbc.chillguys.nebulazone.application.products.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
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
import nbc.chillguys.nebulazone.application.products.dto.response.SearchProductResponse;
import nbc.chillguys.nebulazone.application.products.service.ProductService;
import nbc.chillguys.nebulazone.common.response.CommonPageResponse;
import nbc.chillguys.nebulazone.domain.auth.vo.AuthUser;
import nbc.chillguys.nebulazone.domain.common.validator.image.ImageFile;
import nbc.chillguys.nebulazone.domain.products.entity.ProductTxMethod;

@RestController
@RequiredArgsConstructor
public class ProductController {

	private final ProductService productService;

	@PostMapping(value = "/catalogs/{catalogId}/products", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<ProductResponse> createProduct(
		@AuthenticationPrincipal AuthUser authUser,
		@PathVariable("catalogId") Long catalogId,
		@Valid @RequestPart("product") CreateProductRequest request,
		@RequestPart(value = "images", required = false) List<MultipartFile> multipartFiles) {

		ProductResponse productResponse = productService.createProduct(authUser, catalogId, request,
			multipartFiles);

		return ResponseEntity.status(HttpStatus.CREATED).body(productResponse);
	}

	@PutMapping("/catalogs/{catalogId}/products/{productId}")
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

	@PatchMapping("/catalogs/{catalogId}/products/{productId}/auction-type")
	public ResponseEntity<ProductResponse> changeToAuctionType(
		@AuthenticationPrincipal AuthUser authUser,
		@PathVariable("catalogId") Long catalogId,
		@PathVariable("productId") Long productId,
		@Valid @RequestBody ChangeToAuctionTypeRequest request
	) {
		ProductResponse response = productService.changeToAuctionType(authUser.getId(), catalogId, productId, request);

		return ResponseEntity.ok(response);
	}

	@DeleteMapping("/catalogs/{catalogId}/products/{productId}")
	public ResponseEntity<DeleteProductResponse> deleteProduct(
		@AuthenticationPrincipal AuthUser authUser,
		@PathVariable("catalogId") Long catalogId,
		@PathVariable("productId") Long productId
	) {
		DeleteProductResponse response = productService.deleteProduct(authUser.getId(), catalogId, productId);

		return ResponseEntity.ok(response);
	}

	@PostMapping("/catalogs/{catalogId}/products/{productId}/purchase")
	public ResponseEntity<PurchaseProductResponse> purchaseProduct(
		@AuthenticationPrincipal AuthUser authUser,
		@PathVariable("catalogId") Long catalogId,
		@PathVariable("productId") Long productId
	) {
		PurchaseProductResponse response = productService.purchaseProduct(authUser.getId(), catalogId, productId);

		return ResponseEntity.ok(response);
	}

	@GetMapping("/products")
	public ResponseEntity<CommonPageResponse<SearchProductResponse>> searchProduct(
		@RequestParam(value = "name", required = false) String name,
		@RequestParam("type") ProductTxMethod type,
		@RequestParam(value = "from", required = false) Long priceFrom,
		@RequestParam(value = "to", required = false) Long priceTo,
		@RequestParam(value = "page", defaultValue = "1") Integer page,
		@RequestParam(value = "size", defaultValue = "10") Integer size
	) {
		Page<SearchProductResponse> responses = productService.searchProduct(name, type, priceFrom, priceTo, page,
			size);

		return ResponseEntity.ok(CommonPageResponse.from(responses));
	}

	@GetMapping("/catalogs/{catalogId}/products/{productId}")
	public ResponseEntity<ProductResponse> getProduct(
		@PathVariable("catalogId") Long catalogId,
		@PathVariable("productId") Long productId
	) {
		ProductResponse response = productService.getProduct(catalogId, productId);

		return ResponseEntity.ok(response);
	}
}
