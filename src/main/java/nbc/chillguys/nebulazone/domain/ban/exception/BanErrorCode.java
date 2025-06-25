package nbc.chillguys.nebulazone.domain.ban.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.common.exception.ErrorCode;

@Getter
@RequiredArgsConstructor
public enum BanErrorCode implements ErrorCode {
	BAN_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 밴입니다."),
	ALREADY_BANNED(HttpStatus.BAD_REQUEST, "이미 존재하는 밴입니다");

	private final HttpStatus status;
	private final String message;
}
