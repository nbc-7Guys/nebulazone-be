package nbc.chillguys.nebulazone.application.catalog.dto.response;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import nbc.chillguys.nebulazone.domain.catalog.entity.Catalog;

public record CatalogResponse(
	Long catalogId,
	String catalogName,
	String catalogDescription,
	String catalogType,
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	LocalDateTime createdAt,
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	LocalDateTime modifiedAt
) {
	public static CatalogResponse from(Catalog catalog) {
		return new CatalogResponse(
			catalog.getId(),
			catalog.getName(),
			catalog.getDescription(),
			catalog.getType().name(),
			catalog.getCreatedAt(),
			catalog.getModifiedAt()
		);
	}
}
