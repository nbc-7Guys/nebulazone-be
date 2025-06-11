package nbc.chillguys.nebulazone.application.catalog.controller;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.application.catalog.dto.request.AdminCatalogSearchRequest;
import nbc.chillguys.nebulazone.application.catalog.dto.request.AdminCatalogUpdateRequest;
import nbc.chillguys.nebulazone.application.catalog.dto.response.AdminCatalogResponse;
import nbc.chillguys.nebulazone.application.catalog.service.AdminCatalogService;
import nbc.chillguys.nebulazone.common.response.CommonPageResponse;
import nbc.chillguys.nebulazone.domain.catalog.entity.CatalogType;

@RestController
@RequestMapping("/admin/catalogs")
@RequiredArgsConstructor
public class AdminCatalogController {
	private final AdminCatalogService adminCatalogService;

	@GetMapping
	public ResponseEntity<CommonPageResponse<AdminCatalogResponse>> findCatalogs(
		@RequestParam(value = "keyword", required = false) String keyword,
		@RequestParam(value = "type", required = false) CatalogType type,
		@RequestParam(value = "page", defaultValue = "1") int page,
		@RequestParam(value = "size", defaultValue = "10") int size
	) {
		AdminCatalogSearchRequest request = new AdminCatalogSearchRequest(keyword, type, page, size);
		Pageable pageable = PageRequest.of(page - 1, size);
		CommonPageResponse<AdminCatalogResponse> response = adminCatalogService.findCatalogs(request, pageable);
		return ResponseEntity.ok(response);
	}

	@PatchMapping("/{catalogId}")
	public ResponseEntity<Void> updateCatalog(
		@PathVariable Long catalogId,
		@RequestBody AdminCatalogUpdateRequest request
	) {
		adminCatalogService.updateCatalog(catalogId, request);
		return ResponseEntity.noContent().build();
	}

	@DeleteMapping("/{catalogId}")
	public ResponseEntity<Void> deleteCatalog(@PathVariable Long catalogId) {
		adminCatalogService.deleteCatalog(catalogId);
		return ResponseEntity.noContent().build();
	}
}
