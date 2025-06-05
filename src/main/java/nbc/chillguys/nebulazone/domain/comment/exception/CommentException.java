package nbc.chillguys.nebulazone.domain.comment.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.common.exception.BaseException;

@Getter
@RequiredArgsConstructor
public class CommentException extends BaseException {
	private final CommentErrorCode errorCode;
}
