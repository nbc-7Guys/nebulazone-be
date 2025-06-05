package nbc.chillguys.nebulazone.domain.review.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.common.exception.ErrorCode;

@Getter
@RequiredArgsConstructor
public enum ReviewErrorCode implements ErrorCode {
	REVIEW_NOT_FOUND(HttpStatus.NOT_FOUND, "리뷰를 찾을 수 없습니다."),
	REVIEW_ACCESS_DENIED(HttpStatus.FORBIDDEN, "해당 리뷰에 접근할 수 없습니다.");

	private final HttpStatus status;
	private final String message;
}
