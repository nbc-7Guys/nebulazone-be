package nbc.chillguys.nebulazone.domain.notification.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.common.exception.BaseException;

@Getter
@RequiredArgsConstructor
public class NotificationException extends BaseException {
	private final NotificationErrorCode errorCode;
}
