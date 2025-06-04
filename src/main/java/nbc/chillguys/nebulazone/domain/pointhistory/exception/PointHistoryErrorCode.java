package nbc.chillguys.nebulazone.domain.pointhistory.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.common.exception.ErrorCode;

@Getter
@RequiredArgsConstructor
public enum PointHistoryErrorCode implements ErrorCode {
	INVALID_TYPE(HttpStatus.BAD_REQUEST, "지원하지 않는 타입입니다."),
	POINT_HISTORY_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 포인트 거래 내역입니다."),
	ALREADY_PROCESSED(HttpStatus.BAD_REQUEST, "이미 처리된 요청입니다."),
	NOT_PENDING(HttpStatus.BAD_REQUEST, "대기 중(PENDING) 상태만 처리할 수 있습니다."),
	INSUFFICIENT_BALANCE(HttpStatus.BAD_REQUEST, "포인트가 부족합니다."),
	UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증되지 않은 사용자입니다.");

	private final HttpStatus status;
	private final String message;
}
