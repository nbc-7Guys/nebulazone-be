package nbc.chillguys.nebulazone.application.pointhistory.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import nbc.chillguys.nebulazone.domain.pointhistory.entity.PointHistoryType;

public record PointRequest(
	@NotBlank
	@Min(value = 1, message = "유효하지 않은 포인트 금액입니다.")
	Integer price,
	@NotBlank(message = "포인트 요청 타입은 필수입니다.")
	PointHistoryType type,
	@NotBlank(message = "계좌 번호는 필수입니다.")
	String account
) {
}
