package nbc.chillguys.nebulazone.domain.catalog.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import nbc.chillguys.nebulazone.domain.catalog.dto.AdminCatalogSearchQueryCommand;
import nbc.chillguys.nebulazone.domain.catalog.entity.Catalog;

public interface CustomCatalogAdminRepository {
	Page<Catalog> searchCatalogs(AdminCatalogSearchQueryCommand command, Pageable pageable);
}
