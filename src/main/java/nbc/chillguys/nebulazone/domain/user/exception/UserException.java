package nbc.chillguys.nebulazone.domain.user.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.common.exception.BaseException;

@Getter
@RequiredArgsConstructor
public class UserException extends BaseException {
	private final UserErrorCode errorCode;
}
