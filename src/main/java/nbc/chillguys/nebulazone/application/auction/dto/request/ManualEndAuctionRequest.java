package nbc.chillguys.nebulazone.application.auction.dto.request;

import jakarta.validation.constraints.NotNull;

public record ManualEndAuctionRequest(
	@NotNull(message = "입찰id는 필수 입력 값 입니다.")
	Long bidId
) {
}
