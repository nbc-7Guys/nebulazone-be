package nbc.chillguys.nebulazone.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

	// @ExceptionHandler(BaseException.class)
	// public ResponseEntity<CommonResponse> handleException(BaseException exception) {
	// 	log.error("BaseException : {}", exception.getMessage(), exception);
	//
	// 	return ResponseEntity
	// 		.status(exception.getErrorCode().getStatus())
	// 		.body(CommonResponse.of(exception.getErrorCode()));
	// }

	@ExceptionHandler(Exception.class)
	public ResponseEntity<String> handleException(Exception exception) {
		log.error("exception : {}", exception.getMessage(), exception);

		return ResponseEntity
			.status(HttpStatus.INTERNAL_SERVER_ERROR)
			.body(exception.getMessage());
	}
}
