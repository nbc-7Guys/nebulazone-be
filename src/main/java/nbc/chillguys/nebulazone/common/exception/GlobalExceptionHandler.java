package nbc.chillguys.nebulazone.common.exception;

import org.springframework.core.convert.ConversionFailedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.messaging.handler.annotation.support.MethodArgumentTypeMismatchException;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestCookieException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.fasterxml.jackson.databind.exc.ValueInstantiationException;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import nbc.chillguys.nebulazone.common.response.CommonResponse;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(BaseException.class)
	public ResponseEntity<CommonResponse> handleException(BaseException ex) {
		log.warn("BaseException : {}", ex.getMessage(), ex);

		return ResponseEntity
			.status(ex.getErrorCode().getStatus())
			.body(
				CommonResponse.of(ex.getErrorCode().getStatus().value(), ex.getErrorCode().getMessage()));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<CommonResponse> inputValidationExceptionHandler(
		MethodArgumentNotValidException ex
	) {
		log.warn("ex : {}", ex.getMessage(), ex);
		BindingResult result = ex.getBindingResult();

		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
			.body(CommonResponse.of(ex.getStatusCode().value(), ex.getMessage(), result));
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<CommonResponse> handleHttpMessageNotReadableException(
		HttpMessageNotReadableException httpMessageNotReadableException
	) {
		Throwable cause = httpMessageNotReadableException.getCause();
		if (cause instanceof ValueInstantiationException && cause.getCause() instanceof BaseException baseException) {
			log.warn("ex : {}", baseException.getMessage(), baseException);

			return ResponseEntity.status(baseException.getErrorCode().getStatus())
				.body(CommonResponse.of(baseException.getErrorCode().getStatus().value(), baseException.getMessage()));
		}

		log.warn("ex : {}", httpMessageNotReadableException.getMessage(), httpMessageNotReadableException);
		return ResponseEntity.badRequest()
			.body(CommonResponse.of(HttpStatus.BAD_REQUEST.value(), "잘못된 요청입니다."));
	}

	@ExceptionHandler(MissingRequestCookieException.class)
	public ResponseEntity<CommonResponse> handleMissingCookie(MissingRequestCookieException ex) {
		log.warn("ex : {}", ex.getMessage(), ex);

		return ResponseEntity
			.badRequest()
			.body(CommonResponse.of(HttpStatus.BAD_REQUEST.value(), "잘못된 요청입니다."));
	}

	@ExceptionHandler(MissingServletRequestParameterException.class)
	public ResponseEntity<CommonResponse> handleMissingServletRequestParameter(
		MissingServletRequestParameterException ex) {
		log.warn("MissingServletRequestParameterException : {}", ex.getMessage(), ex);
		return ResponseEntity.badRequest().body(CommonResponse.of(HttpStatus.BAD_REQUEST.value(), "필수 파라미터가 누락되었습니다."));
	}

	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<CommonResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
		log.warn("MethodArgumentTypeMismatchException : {}", ex.getMessage(), ex);
		return ResponseEntity.badRequest()
			.body(CommonResponse.of(HttpStatus.BAD_REQUEST.value(), "파라미터 타입이 올바르지 않습니다."));
	}

	@ExceptionHandler(MissingRequestHeaderException.class)
	public ResponseEntity<CommonResponse> handleMissingRequestHeader(MissingRequestHeaderException ex) {
		log.warn("MissingRequestHeaderException : {}", ex.getMessage(), ex);
		return ResponseEntity.badRequest().body(CommonResponse.of(HttpStatus.BAD_REQUEST.value(), "필수 헤더가 누락되었습니다."));
	}

	@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
	public ResponseEntity<CommonResponse> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
		log.warn("HttpRequestMethodNotSupportedException : {}", ex.getMessage(), ex);
		return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
			.body(CommonResponse.of(HttpStatus.METHOD_NOT_ALLOWED.value(), "지원하지 않는 HTTP 메서드입니다."));
	}

	@ExceptionHandler(ConstraintViolationException.class)
	public ResponseEntity<CommonResponse> handleConstraintViolation(ConstraintViolationException ex) {
		log.warn("ConstraintViolationException : {}", ex.getMessage(), ex);
		return ResponseEntity.badRequest().body(CommonResponse.of(HttpStatus.BAD_REQUEST.value(), "요청 값이 유효하지 않습니다."));
	}

	@ExceptionHandler(BindException.class)
	public ResponseEntity<CommonResponse> handleBindException(BindException ex) {
		log.warn("BindException : {}", ex.getMessage(), ex);
		return ResponseEntity.badRequest().body(CommonResponse.of(HttpStatus.BAD_REQUEST.value(), "요청 데이터 바인딩 오류입니다."));
	}

	@ExceptionHandler(ConversionFailedException.class)
	public ResponseEntity<CommonResponse> handleConversionFailed(ConversionFailedException ex) {
		log.warn("ConversionFailedException : {}", ex.getMessage(), ex);
		return ResponseEntity.badRequest()
			.body(CommonResponse.of(HttpStatus.BAD_REQUEST.value(), "요청 값의 형식이 올바르지 않습니다."));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<CommonResponse> handleAllExceptions(Exception ex) {
		log.error("Unhandled exception: ", ex);
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
			.body(CommonResponse.of(HttpStatus.INTERNAL_SERVER_ERROR.value(), "서버 내부 오류가 발생했습니다."));
	}

}
