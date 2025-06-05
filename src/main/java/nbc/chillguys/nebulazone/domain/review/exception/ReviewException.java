package nbc.chillguys.nebulazone.domain.review.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.common.exception.BaseException;

@Getter
@RequiredArgsConstructor
public class ReviewException extends BaseException {
	private final ReviewErrorCode errorCode;
}
