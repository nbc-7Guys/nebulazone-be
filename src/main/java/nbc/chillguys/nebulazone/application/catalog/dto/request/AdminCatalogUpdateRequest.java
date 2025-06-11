package nbc.chillguys.nebulazone.application.catalog.dto.request;

import nbc.chillguys.nebulazone.domain.catalog.entity.CatalogType;

public record AdminCatalogUpdateRequest(
	String name,
	String description,
	CatalogType type
) {
}
