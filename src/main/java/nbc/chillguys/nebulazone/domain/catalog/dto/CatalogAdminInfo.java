package nbc.chillguys.nebulazone.domain.catalog.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import nbc.chillguys.nebulazone.domain.catalog.entity.Catalog;

public record CatalogAdminInfo(
	Long catalogId,
	String name,
	String description,
	String type,
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	LocalDateTime createdAt,
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	LocalDateTime modifiedAt
) {
	public static CatalogAdminInfo from(Catalog catalog) {
		return new CatalogAdminInfo(
			catalog.getId(),
			catalog.getName(),
			catalog.getDescription(),
			catalog.getType().name(),
			catalog.getCreatedAt(),
			catalog.getModifiedAt()
		);
	}
}
