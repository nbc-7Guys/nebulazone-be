package nbc.chillguys.nebulazone.application.products.contreoller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.application.products.dto.request.CreateProductRequest;
import nbc.chillguys.nebulazone.application.products.dto.response.ProductResponse;
import nbc.chillguys.nebulazone.application.products.service.ProductService;
import nbc.chillguys.nebulazone.domain.auth.vo.AuthUser;
import nbc.chillguys.nebulazone.domain.common.validator.image.ImageFile;

@RestController
@RequiredArgsConstructor
public class ProductController {

	private final ProductService productService;

	@PostMapping(value = "/catalogs/{catalogId}/products",
		consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<ProductResponse> createProduct(
		@AuthenticationPrincipal AuthUser authUser,
		@PathVariable("catalogId") Long catalogId,
		@Valid @RequestPart("product") CreateProductRequest request,
		@ImageFile @RequestPart(value = "images", required = false) List<MultipartFile> multipartFiles) {

		ProductResponse productResponse = productService.createProduct(authUser, catalogId, request,
			multipartFiles);

		return ResponseEntity.status(HttpStatus.CREATED).body(productResponse);
	}
}
