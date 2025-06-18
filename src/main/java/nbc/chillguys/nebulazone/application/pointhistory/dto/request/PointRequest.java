package nbc.chillguys.nebulazone.application.pointhistory.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import nbc.chillguys.nebulazone.domain.pointhistory.entity.PointHistoryType;

public record PointRequest(
	@NotNull(message = "포인트 금액은 필수입니다.")
	@Min(value = 1, message = "유효하지 않은 포인트 금액입니다.")
	Long price,
	@NotNull(message = "포인트 요청 타입은 필수입니다.")
	PointHistoryType type,
	@NotBlank(message = "계좌 번호는 필수입니다.")
	String account
) {
}
