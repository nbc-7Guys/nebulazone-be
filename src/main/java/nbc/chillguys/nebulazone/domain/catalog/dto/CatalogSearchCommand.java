package nbc.chillguys.nebulazone.domain.catalog.dto;

import nbc.chillguys.nebulazone.domain.catalog.entity.CatalogType;

public record CatalogSearchCommand(
	String keyword,
	String type,
	int page,
	int size
) {
	public static CatalogSearchCommand of(String keyword, CatalogType type, int page, int size) {
		return new CatalogSearchCommand(keyword, type.name(), page, size);
	}
}
