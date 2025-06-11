package nbc.chillguys.nebulazone.application.auction.dto.request;

import jakarta.validation.constraints.NotNull;

public record ManualEndAuctionRequest(
	@NotNull(message = "입찰 id값은 필수 입력 값 입니다.")
	Long bidId,

	@NotNull(message = "상품 id값은 필수 입력 값 입니다.")
	Long productId
) {
}
