package nbc.chillguys.nebulazone.domain.post.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.common.exception.BaseException;

@Getter
@RequiredArgsConstructor
public class PostException extends BaseException {
	private final PostErrorCode errorCode;
}
