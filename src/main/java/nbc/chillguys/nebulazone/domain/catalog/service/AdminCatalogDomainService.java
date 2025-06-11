package nbc.chillguys.nebulazone.domain.catalog.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.application.catalog.dto.request.AdminCatalogUpdateRequest;
import nbc.chillguys.nebulazone.domain.catalog.dto.AdminCatalogInfo;
import nbc.chillguys.nebulazone.domain.catalog.dto.AdminCatalogSearchQueryCommand;
import nbc.chillguys.nebulazone.domain.catalog.entity.Catalog;
import nbc.chillguys.nebulazone.domain.catalog.exception.CatalogErrorCode;
import nbc.chillguys.nebulazone.domain.catalog.exception.CatalogException;
import nbc.chillguys.nebulazone.domain.catalog.repository.CatalogRepository;
import nbc.chillguys.nebulazone.domain.catalog.repository.CustomCatalogAdminRepository;

@Service
@RequiredArgsConstructor
public class AdminCatalogDomainService {
	private final CustomCatalogAdminRepository adminCatalogQueryRepository;
	private final CatalogRepository catalogRepository;

	public Page<AdminCatalogInfo> findCatalogs(AdminCatalogSearchQueryCommand command, Pageable pageable) {
		return adminCatalogQueryRepository.searchCatalogs(command, pageable)
			.map(AdminCatalogInfo::from);
	}

	public void updateCatalog(Long catalogId, AdminCatalogUpdateRequest request) {

		Catalog catalog = findCatalogId(catalogId);

		catalog.update(request.name(), request.description(), request.type());
	}

	public void deleteCatalog(Long catalogId) {

		Catalog catalog = findCatalogId(catalogId);

		catalogRepository.delete(catalog);
	}

	public Catalog findCatalogId(Long catalogId) {
		return catalogRepository.findById(catalogId)
			.orElseThrow(() -> new CatalogException(CatalogErrorCode.CATALOG_NOT_FOUND));
	}
}
