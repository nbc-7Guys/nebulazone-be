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
import nbc.chillguys.nebulazone.application.catalog.dto.request.CatalogAdminSearchRequest;
import nbc.chillguys.nebulazone.application.catalog.dto.request.CatalogAdminUpdateRequest;
import nbc.chillguys.nebulazone.application.catalog.dto.response.CatalogAdminResponse;
import nbc.chillguys.nebulazone.application.catalog.service.CatalogAdminService;
import nbc.chillguys.nebulazone.common.response.CommonPageResponse;
import nbc.chillguys.nebulazone.domain.catalog.entity.CatalogType;

@RestController
@RequestMapping("/admin/catalogs")
@RequiredArgsConstructor
public class CatalogAdminController {
	private final CatalogAdminService catalogAdminService;

	@GetMapping
	public ResponseEntity<CommonPageResponse<CatalogAdminResponse>> findCatalogs(
		@RequestParam(value = "keyword", required = false) String keyword,
		@RequestParam(value = "type", required = false) CatalogType type,
		@RequestParam(value = "page", defaultValue = "1") int page,
		@RequestParam(value = "size", defaultValue = "10") int size
	) {
		CatalogAdminSearchRequest request = new CatalogAdminSearchRequest(keyword, type, page, size);
		Pageable pageable = PageRequest.of(page - 1, size);
		CommonPageResponse<CatalogAdminResponse> response = catalogAdminService.findCatalogs(request, pageable);
		return ResponseEntity.ok(response);
	}

	@PatchMapping("/{catalogId}")
	public ResponseEntity<Void> updateCatalog(
		@PathVariable Long catalogId,
		@RequestBody CatalogAdminUpdateRequest request
	) {
		catalogAdminService.updateCatalog(catalogId, request);
		return ResponseEntity.noContent().build();
	}

	@DeleteMapping("/{catalogId}")
	public ResponseEntity<Void> deleteCatalog(@PathVariable Long catalogId) {
		catalogAdminService.deleteCatalog(catalogId);
		return ResponseEntity.noContent().build();
	}
}
