package nbc.chillguys.nebulazone.application.catalog.dto.response;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import nbc.chillguys.nebulazone.domain.catalog.vo.CatalogDocument;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record SearchCatalogResponse(
	Long catalogId,
	String catalogName,
	String catalogDescription,
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	LocalDateTime createdAt,
	String catalogType,
	String manufacturer,
	String chipset,
	String formFactor,
	String socket
) {
	public static SearchCatalogResponse from(CatalogDocument catalogDocument) {
		return new SearchCatalogResponse(
			catalogDocument.catalogId(),
			catalogDocument.name(),
			catalogDocument.description(),
			catalogDocument.createdAt(),
			catalogDocument.type(),
			catalogDocument.manufacturer(),
			catalogDocument.chipset(),
			catalogDocument.formFactor(),
			catalogDocument.socket()
		);
	}
}
