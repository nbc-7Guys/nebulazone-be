package nbc.chillguys.nebulazone.domain.post.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.common.exception.ErrorCode;

@Getter
@RequiredArgsConstructor
public enum PostErrorCode implements ErrorCode {
	INVALID_POST_TYPE(HttpStatus.BAD_REQUEST, "유효하지 않은 게시글 타입 입니다."),
	POST_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 게시글입니다."),
	NOT_POST_OWNER(HttpStatus.FORBIDDEN, "게시글 작성자가 아닙니다.");

	private final HttpStatus status;
	private final String message;
}
