package nbc.chillguys.nebulazone.domain.user.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.common.exception.ErrorCode;

@Getter
@RequiredArgsConstructor
public enum UserErrorCode implements ErrorCode {
	WRONG_ROLES(HttpStatus.BAD_REQUEST, "유저 권한을 잘못 입력하였습니다."),
	WRONG_PASSWORD(HttpStatus.UNAUTHORIZED, "비밀번호가 틀렸습니다."),
	USER_NOT_FOUND(HttpStatus.NOT_FOUND, "유저를 찾을 수 없습니다."),
	ALREADY_EXISTS_EMAIL(HttpStatus.CONFLICT, "이미 존재하는 이메일이 있습니다."),
	ALREADY_EXISTS_NICKNAME(HttpStatus.CONFLICT, "이미 존재하는 닉네임이 있습니다.");

	private final HttpStatus status;
	private final String message;
}
