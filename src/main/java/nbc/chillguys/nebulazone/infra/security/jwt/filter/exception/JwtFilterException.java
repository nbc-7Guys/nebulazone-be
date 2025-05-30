package nbc.chillguys.nebulazone.infra.security.jwt.filter.exception;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;

import lombok.Getter;

@Getter
public class JwtFilterException extends AuthenticationException {
	private final JwtFilterErrorCode errorCode;

	public JwtFilterException(JwtFilterErrorCode jwtFilterErrorCode) {
		super(jwtFilterErrorCode.getMessage());
		this.errorCode = jwtFilterErrorCode;
	}

	public HttpStatus getStatus() {
		return this.errorCode.getStatus();
	}
}
