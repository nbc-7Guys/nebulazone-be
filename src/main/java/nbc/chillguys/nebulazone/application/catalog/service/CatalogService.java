package nbc.chillguys.nebulazone.application.catalog.service;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.application.catalog.dto.response.CatalogResponse;
import nbc.chillguys.nebulazone.application.catalog.dto.response.SearchCatalogResponse;
import nbc.chillguys.nebulazone.domain.catalog.dto.CatalogSearchCommand;
import nbc.chillguys.nebulazone.domain.catalog.entity.Catalog;
import nbc.chillguys.nebulazone.domain.catalog.entity.CatalogType;
import nbc.chillguys.nebulazone.domain.catalog.service.CatalogDomainService;
import nbc.chillguys.nebulazone.domain.catalog.vo.CatalogDocument;

@Service
@RequiredArgsConstructor
public class CatalogService {
	private final CatalogDomainService catalogDomainService;

	public Page<SearchCatalogResponse> searchCatalog(String keyword, CatalogType type, int page, int size) {
		CatalogSearchCommand command = CatalogSearchCommand.of(keyword, type, page, size);

		Page<CatalogDocument> catalogDocuments = catalogDomainService.searchCatalog(command);

		return catalogDocuments.map(SearchCatalogResponse::from);
	}

	public CatalogResponse getCatalog(Long catalogId) {
		Catalog catalog = catalogDomainService.getCatalogById(catalogId);

		return CatalogResponse.from(catalog);
	}
}
