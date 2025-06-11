package nbc.chillguys.nebulazone.application.catalog.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.application.catalog.dto.request.AdminCatalogSearchRequest;
import nbc.chillguys.nebulazone.application.catalog.dto.request.AdminCatalogUpdateRequest;
import nbc.chillguys.nebulazone.application.catalog.dto.response.AdminCatalogResponse;
import nbc.chillguys.nebulazone.common.response.CommonPageResponse;
import nbc.chillguys.nebulazone.domain.catalog.dto.AdminCatalogInfo;
import nbc.chillguys.nebulazone.domain.catalog.dto.AdminCatalogSearchQueryCommand;
import nbc.chillguys.nebulazone.domain.catalog.service.AdminCatalogDomainService;

@Service
@RequiredArgsConstructor
public class AdminCatalogService {
	private final AdminCatalogDomainService adminCatalogDomainService;

	public CommonPageResponse<AdminCatalogResponse> findCatalogs(AdminCatalogSearchRequest request, Pageable pageable) {
		AdminCatalogSearchQueryCommand command = new AdminCatalogSearchQueryCommand(
			request.keyword(),
			request.type()
		);
		Page<AdminCatalogInfo> infoPage = adminCatalogDomainService.findCatalogs(command, pageable);
		return CommonPageResponse.from(infoPage.map(AdminCatalogResponse::from));
	}

	public void updateCatalog(Long catalogId, AdminCatalogUpdateRequest request) {
		adminCatalogDomainService.updateCatalog(catalogId, request);
	}

	public void deleteCatalog(Long catalogId) {
		adminCatalogDomainService.deleteCatalog(catalogId);
	}
}
