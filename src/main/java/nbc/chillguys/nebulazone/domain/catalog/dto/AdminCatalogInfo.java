package nbc.chillguys.nebulazone.domain.catalog.dto;

import java.time.LocalDateTime;

import nbc.chillguys.nebulazone.domain.catalog.entity.Catalog;

public record AdminCatalogInfo(
	Long catalogId,
	String name,
	String description,
	String type,
	LocalDateTime createdAt,
	LocalDateTime modifiedAt
) {
	public static AdminCatalogInfo from(Catalog catalog) {
		return new AdminCatalogInfo(
			catalog.getId(),
			catalog.getName(),
			catalog.getDescription(),
			catalog.getType().name(),
			catalog.getCreatedAt(),
			catalog.getModifiedAt()
		);
	}
}
