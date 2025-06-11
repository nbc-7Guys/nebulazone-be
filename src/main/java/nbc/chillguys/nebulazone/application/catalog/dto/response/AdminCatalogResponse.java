package nbc.chillguys.nebulazone.application.catalog.dto.response;

import java.time.LocalDateTime;

import nbc.chillguys.nebulazone.domain.catalog.dto.AdminCatalogInfo;

public record AdminCatalogResponse(
	Long catalogId,
	String name,
	String description,
	String type,
	LocalDateTime createdAt,
	LocalDateTime modifiedAt
) {
	public static AdminCatalogResponse from(AdminCatalogInfo info) {
		return new AdminCatalogResponse(
			info.catalogId(),
			info.name(),
			info.description(),
			info.type(),
			info.createdAt(),
			info.modifiedAt()
		);
	}
}
