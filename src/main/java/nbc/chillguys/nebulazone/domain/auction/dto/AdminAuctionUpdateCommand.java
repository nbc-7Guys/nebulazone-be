package nbc.chillguys.nebulazone.domain.auction.dto;

import nbc.chillguys.nebulazone.application.auction.dto.request.AdminAuctionUpdateRequest;

public record AdminAuctionUpdateCommand(
	Long startPrice,
	Long currentPrice,
	java.time.LocalDateTime endTime,
	Boolean isWon
) {
	public static AdminAuctionUpdateCommand from(AdminAuctionUpdateRequest request) {
		return new AdminAuctionUpdateCommand(
			request.startPrice(),
			request.currentPrice(),
			request.endTime(),
			request.isWon()
		);
	}
}
