package nbc.chillguys.nebulazone.application.catalog.dto.request;

import nbc.chillguys.nebulazone.domain.catalog.entity.CatalogType;

public record AdminCatalogSearchRequest(
	String keyword,
	CatalogType type,
	int page,
	int size
) {
}
