package nbc.chillguys.nebulazone.domain.comment.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.common.exception.ErrorCode;

@Getter
@RequiredArgsConstructor
public enum CommentErrorCode implements ErrorCode {
	COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 댓글입니다.");

	private final HttpStatus status;
	private final String message;
}
