package nbc.chillguys.nebulazone.domain.catalog.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.common.exception.ErrorCode;

@Getter
@RequiredArgsConstructor
public enum CatalogErrorCode implements ErrorCode {
	CATALOG_NOT_FOUND(HttpStatus.NOT_FOUND, "카탈로그를 찾을 수 없습니다.");

	private final HttpStatus status;
	private final String message;
}
