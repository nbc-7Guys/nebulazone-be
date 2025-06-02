package nbc.chillguys.nebulazone.domain.products.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.common.exception.BaseException;

@Getter
@RequiredArgsConstructor
public class ProductException extends BaseException {
	private final ProductErrorCode errorCode;
}
