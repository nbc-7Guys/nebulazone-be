package nbc.chillguys.nebulazone.infra.security.jwt.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.common.exception.ErrorCode;

@Getter
@RequiredArgsConstructor
public enum JwtTokenErrorCode implements ErrorCode {
	REFRESH_TOKEN_EXPIRED(HttpStatus.FORBIDDEN, "refresh token이 만료되었습니다."),
	BAD_REQUEST(HttpStatus.BAD_REQUEST, "옳바르지 않은 요청입니다.."),
	EXPIRED_JWT_TOKEN(HttpStatus.FORBIDDEN, "만료된 JWT 토큰입니다."),
	NOT_VALID_JWT_TOKEN(HttpStatus.FORBIDDEN, "옳바르지 않은 JWT 토큰입니다."),
	NOT_VALID_SIGNATURE(HttpStatus.FORBIDDEN, "서명이 옳바르지 않습니다."),
	NOT_VALID_CONTENT(HttpStatus.FORBIDDEN, "내용이 옳바르지 않습니다.");

	private final HttpStatus status;
	private final String message;
}
