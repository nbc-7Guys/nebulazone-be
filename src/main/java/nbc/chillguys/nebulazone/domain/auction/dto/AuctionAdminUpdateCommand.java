package nbc.chillguys.nebulazone.domain.auction.dto;

import java.time.LocalDateTime;

import nbc.chillguys.nebulazone.application.auction.dto.request.AuctionAdminUpdateRequest;

public record AuctionAdminUpdateCommand(
	Long startPrice,
	Long currentPrice,
	LocalDateTime endTime,
	Boolean isWon
) {
	public static AuctionAdminUpdateCommand from(AuctionAdminUpdateRequest request) {
		return new AuctionAdminUpdateCommand(
			request.startPrice(),
			request.currentPrice(),
			request.endTime(),
			request.isWon()
		);
	}
}
