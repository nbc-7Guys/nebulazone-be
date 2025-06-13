package nbc.chillguys.nebulazone.application.auction.dto.request;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotNull;

public record AuctionAdminUpdateRequest(
	@NotNull
	Long startPrice,

	@NotNull
	Long currentPrice,

	@NotNull
	LocalDateTime endTime,
	Boolean isWon
) {
}
