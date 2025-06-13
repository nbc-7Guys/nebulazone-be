package nbc.chillguys.nebulazone.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestCookieException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.fasterxml.jackson.databind.exc.ValueInstantiationException;

import lombok.extern.slf4j.Slf4j;
import nbc.chillguys.nebulazone.common.response.CommonResponse;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(BaseException.class)
	public ResponseEntity<CommonResponse> handleException(BaseException exception) {
		log.error("BaseException : {}", exception.getMessage(), exception);

		return ResponseEntity
			.status(exception.getErrorCode().getStatus())
			.body(
				CommonResponse.of(exception.getErrorCode().getStatus().value(), exception.getErrorCode().getMessage()));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<CommonResponse> inputValidationExceptionHandler(
		MethodArgumentNotValidException exception
	) {
		log.error("exception : {}", exception.getMessage(), exception);
		BindingResult result = exception.getBindingResult();

		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
			.body(CommonResponse.of(exception.getStatusCode().value(), exception.getMessage(), result));
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<CommonResponse> handleHttpMessageNotReadableException(
		HttpMessageNotReadableException httpMessageNotReadableException
	) {
		Throwable cause = httpMessageNotReadableException.getCause();
		if (cause instanceof ValueInstantiationException && cause.getCause() instanceof BaseException baseException) {
			log.error("exception : {}", baseException.getMessage(), baseException);

			return ResponseEntity.status(baseException.getErrorCode().getStatus())
				.body(CommonResponse.of(baseException.getErrorCode().getStatus().value(), baseException.getMessage()));
		}

		log.error("exception : {}", httpMessageNotReadableException.getMessage(), httpMessageNotReadableException);
		return ResponseEntity.badRequest()
			.body(CommonResponse.of(HttpStatus.BAD_REQUEST.value(), "잘못된 요청입니다."));
	}

	@ExceptionHandler(MissingRequestCookieException.class)
	public ResponseEntity<CommonResponse> handleMissingCookie(MissingRequestCookieException exception) {
		log.error("exception : {}", exception.getMessage(), exception);

		return ResponseEntity
			.badRequest()
			.body(CommonResponse.of(HttpStatus.BAD_REQUEST.value(), "잘못된 요청입니다."));
	}
}
