package nbc.chillguys.nebulazone.infra.security.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.common.exception.ErrorCode;

@Getter
@RequiredArgsConstructor
public enum JwtTokenErrorCode implements ErrorCode {
	REFRESH_TOKEN_EXPIRED(HttpStatus.FORBIDDEN, "refresh token이 만료되었습니다.");

	private final HttpStatus status;
	private final String message;
}
