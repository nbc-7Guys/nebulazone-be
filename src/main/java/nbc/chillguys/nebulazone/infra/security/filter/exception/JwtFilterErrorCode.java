package nbc.chillguys.nebulazone.infra.security.filter.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.common.exception.ErrorCode;

@Getter
@RequiredArgsConstructor
public enum JwtFilterErrorCode implements ErrorCode {
	TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "토큰이 만료되었습니다."),
	EXPIRED_JWT_TOKEN(HttpStatus.FORBIDDEN, "만료된 JWT 토큰입니다."),
	BAD_REQUEST(HttpStatus.BAD_REQUEST, "올바르지 않은 요청입니다.."),
	NOT_VALID_JWT_TOKEN(HttpStatus.FORBIDDEN, "올바르지 않은 JWT 토큰입니다."),
	NOT_VALID_SIGNATURE(HttpStatus.FORBIDDEN, "서명이 올바르지 않습니다."),
	NOT_VALID_CONTENT(HttpStatus.FORBIDDEN, "내용이 올바르지 않습니다."),
	MALFORMED_JWT_REQUEST(HttpStatus.UNAUTHORIZED, "요청 형태가 잘못 되었습니다.");

	private final HttpStatus status;
	private final String message;
}
