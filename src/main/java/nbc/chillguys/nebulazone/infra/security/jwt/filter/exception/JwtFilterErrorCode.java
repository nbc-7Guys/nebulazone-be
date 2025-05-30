package nbc.chillguys.nebulazone.infra.security.jwt.filter.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.common.exception.ErrorCode;

@Getter
@RequiredArgsConstructor
public enum JwtFilterErrorCode implements ErrorCode {
	TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "토큰이 만료되었습니다."),
	EMPTY_TOKEN(HttpStatus.UNAUTHORIZED, "헤더에 토큰을 포함하고 있지 않습니다."),
	MALFORMED_JWT_REQUEST(HttpStatus.UNAUTHORIZED, "요청 형태가 잘못 되었습니다.");

	private final HttpStatus status;
	private final String message;
}
