package nbc.chillguys.nebulazone.domain.user.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.common.exception.ErrorCode;

@Getter
@RequiredArgsConstructor
public enum UserErrorCode implements ErrorCode {
	;

	private final int status;
	private final String message;
}
