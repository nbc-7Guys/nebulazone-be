package nbc.chillguys.nebulazone.common.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.validation.BindingResult;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CommonResponse {

	private final int status;
	private final String message;
	private final LocalDateTime timestamp;
	private final List<FieldError> errors;

	public static CommonResponse of(int status, String message) {
		return CommonResponse.builder()
			.status(status)
			.message(message)
			.timestamp(LocalDateTime.now())
			.build();
	}

	public static CommonResponse of(int status, String message, BindingResult bindingResult) {
		return CommonResponse.builder()
			.status(status)
			.message(message)
			.timestamp(LocalDateTime.now())
			.errors(FieldError.of(bindingResult))
			.build();
	}

	@Getter
	@AllArgsConstructor
	public static class FieldError {

		private String field;
		private String rejectedValue;
		private String reason;

		public static List<FieldError> of(BindingResult bindingResult) {
			return bindingResult.getFieldErrors()
				.stream()
				.map(error -> new FieldError(error.getField(),
					error.getRejectedValue() == null ? "" : error.getRejectedValue().toString(),
					error.getDefaultMessage()))
				.collect(Collectors.toList());
		}
	}
}
