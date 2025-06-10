package nbc.chillguys.nebulazone.domain.catalog.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.common.exception.BaseException;

@Getter
@RequiredArgsConstructor
public class CatalogException extends BaseException {
	private final CatalogErrorCode errorCode;
}
