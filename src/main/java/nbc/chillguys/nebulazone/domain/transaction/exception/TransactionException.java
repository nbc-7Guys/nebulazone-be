package nbc.chillguys.nebulazone.domain.transaction.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.common.exception.BaseException;

@Getter
@RequiredArgsConstructor
public class TransactionException extends BaseException {
	private final TransactionErrorCode errorCode;
}
