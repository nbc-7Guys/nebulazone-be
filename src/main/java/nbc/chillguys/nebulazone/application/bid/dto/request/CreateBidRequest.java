package nbc.chillguys.nebulazone.application.bid.dto.request;

import jakarta.validation.constraints.NotNull;

public record CreateBidRequest(
	@NotNull(message = "입찰가는 필수 입력값 입니다.")
	Long price
) {
}
