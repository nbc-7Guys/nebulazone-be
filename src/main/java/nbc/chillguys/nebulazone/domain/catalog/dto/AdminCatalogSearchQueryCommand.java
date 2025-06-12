package nbc.chillguys.nebulazone.domain.catalog.dto;

import nbc.chillguys.nebulazone.domain.catalog.entity.CatalogType;

public record AdminCatalogSearchQueryCommand(
	String keyword,
	CatalogType type
) {
}
