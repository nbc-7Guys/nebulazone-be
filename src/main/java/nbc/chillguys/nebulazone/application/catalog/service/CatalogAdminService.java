package nbc.chillguys.nebulazone.application.catalog.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.application.catalog.dto.request.CatalogAdminSearchRequest;
import nbc.chillguys.nebulazone.application.catalog.dto.request.CatalogAdminUpdateRequest;
import nbc.chillguys.nebulazone.application.catalog.dto.response.CatalogAdminResponse;
import nbc.chillguys.nebulazone.common.response.CommonPageResponse;
import nbc.chillguys.nebulazone.domain.catalog.dto.CatalogAdminInfo;
import nbc.chillguys.nebulazone.domain.catalog.dto.CatalogAdminSearchQueryCommand;
import nbc.chillguys.nebulazone.domain.catalog.service.CatalogAdminDomainService;

@Service
@RequiredArgsConstructor
public class CatalogAdminService {
	private final CatalogAdminDomainService catalogAdminDomainService;

	public CommonPageResponse<CatalogAdminResponse> findCatalogs(CatalogAdminSearchRequest request, Pageable pageable) {
		CatalogAdminSearchQueryCommand command = new CatalogAdminSearchQueryCommand(
			request.keyword(),
			request.type()
		);
		Page<CatalogAdminInfo> infoPage = catalogAdminDomainService.findCatalogs(command, pageable);
		return CommonPageResponse.from(infoPage.map(CatalogAdminResponse::from));
	}

	public void updateCatalog(Long catalogId, CatalogAdminUpdateRequest request) {
		catalogAdminDomainService.updateCatalog(catalogId, request);
	}

	public void deleteCatalog(Long catalogId) {
		catalogAdminDomainService.deleteCatalog(catalogId);
	}
}
