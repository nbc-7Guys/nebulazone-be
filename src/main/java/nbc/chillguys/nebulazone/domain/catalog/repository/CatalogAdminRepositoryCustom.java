package nbc.chillguys.nebulazone.domain.catalog.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import nbc.chillguys.nebulazone.domain.catalog.dto.CatalogAdminSearchQueryCommand;
import nbc.chillguys.nebulazone.domain.catalog.entity.Catalog;

public interface CatalogAdminRepositoryCustom {
	Page<Catalog> searchCatalogs(CatalogAdminSearchQueryCommand command, Pageable pageable);
}
