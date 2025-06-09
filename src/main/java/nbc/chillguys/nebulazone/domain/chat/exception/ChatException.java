package nbc.chillguys.nebulazone.domain.chat.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.common.exception.BaseException;

@Getter
@RequiredArgsConstructor
public class ChatException extends BaseException {
	private final ChatErrorCode errorCode;
}
