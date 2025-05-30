package nbc.chillguys.nebulazone.infra.security.jwt.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.common.exception.BaseException;

@Getter
@RequiredArgsConstructor
public class JwtTokenException extends BaseException {
	private final JwtTokenErrorCode errorCode;

	public String getMessage() {
		return this.errorCode.getMessage();
	}
}
