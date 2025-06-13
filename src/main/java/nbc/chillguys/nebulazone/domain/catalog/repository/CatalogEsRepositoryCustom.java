package nbc.chillguys.nebulazone.domain.catalog.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import nbc.chillguys.nebulazone.domain.catalog.vo.CatalogDocument;

public interface CatalogEsRepositoryCustom {
	Page<CatalogDocument> searchCatalog(String keyword, String type, Pageable pageable);
}
