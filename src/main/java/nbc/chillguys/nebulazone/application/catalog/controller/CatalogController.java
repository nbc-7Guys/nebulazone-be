package nbc.chillguys.nebulazone.application.catalog.controller;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.application.catalog.dto.response.CatalogResponse;
import nbc.chillguys.nebulazone.application.catalog.dto.response.SearchCatalogResponse;
import nbc.chillguys.nebulazone.application.catalog.service.CatalogService;
import nbc.chillguys.nebulazone.common.response.CommonPageResponse;
import nbc.chillguys.nebulazone.domain.catalog.entity.CatalogType;

@RestController
@RequestMapping("/catalogs")
@RequiredArgsConstructor
public class CatalogController {
	private final CatalogService catalogService;

	@GetMapping
	public ResponseEntity<CommonPageResponse<SearchCatalogResponse>> searchCatalog(
		@RequestParam(value = "keyword", required = false) String keyword,
		@RequestParam("type") CatalogType type,
		@RequestParam(value = "page", defaultValue = "1") Integer page,
		@RequestParam(value = "size", defaultValue = "10") Integer size
	) {
		Page<SearchCatalogResponse> responses = catalogService.searchCatalog(keyword, type, page, size);

		return ResponseEntity.ok(CommonPageResponse.from(responses));
	}

	@GetMapping("/{catalogId}")
	public ResponseEntity<CatalogResponse> getCatalog(@PathVariable("catalogId") Long catalogId) {
		CatalogResponse response = catalogService.getCatalog(catalogId);

		return ResponseEntity.ok(response);
	}
}
