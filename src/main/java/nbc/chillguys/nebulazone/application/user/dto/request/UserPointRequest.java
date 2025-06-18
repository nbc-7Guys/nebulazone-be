package nbc.chillguys.nebulazone.application.user.dto.request;

import jakarta.validation.constraints.Min;

public record UserPointRequest(
	@Min(value = 1, message = "유효하지 않은 포인트 금액입니다.")
	Long price,
	String account,
	String depositorName
) {
}
