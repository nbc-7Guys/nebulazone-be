package nbc.chillguys.nebulazone.domain.notification.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.common.exception.ErrorCode;

@Getter
@RequiredArgsConstructor
public enum NotificationErrorCode implements ErrorCode {
	NOTIFICATION_NOT_FOUND(HttpStatus.BAD_REQUEST, "알림이 존재하지 않습니다.");

	private final HttpStatus status;
	private final String message;
}
