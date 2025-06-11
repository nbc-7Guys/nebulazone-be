package nbc.chillguys.nebulazone.domain.transaction.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.common.exception.ErrorCode;

@Getter
@RequiredArgsConstructor
public enum TransactionErrorCode implements ErrorCode {
	INVALID_TX_METHOD(HttpStatus.BAD_REQUEST, "유효하지 않은 거래 타입 입니다."),
	NOT_FOUNT_TRANSACTION(HttpStatus.NOT_FOUND, "존재하지 않는 거래목록 입니다.");

	private final HttpStatus status;
	private final String message;
}
