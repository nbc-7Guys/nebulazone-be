package nbc.chillguys.nebulazone.application.catalog.dto.response;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import nbc.chillguys.nebulazone.domain.catalog.dto.CatalogAdminInfo;

public record CatalogAdminResponse(
	Long catalogId,
	String name,
	String description,
	String type,
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	LocalDateTime createdAt,
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	LocalDateTime modifiedAt
) {
	public static CatalogAdminResponse from(CatalogAdminInfo info) {
		return new CatalogAdminResponse(
			info.catalogId(),
			info.name(),
			info.description(),
			info.type(),
			info.createdAt(),
			info.modifiedAt()
		);
	}
}
