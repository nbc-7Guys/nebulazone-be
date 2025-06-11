package nbc.chillguys.nebulazone.application.auction.dto.response;

import java.time.LocalDateTime;

import nbc.chillguys.nebulazone.domain.auction.dto.AdminAuctionInfo;

public record AdminAuctionResponse(
	Long auctionId,
	String productName,
	Long startPrice,
	Long currentPrice,
	LocalDateTime endTime,
	Boolean isWon,
	Boolean deleted,
	LocalDateTime deletedAt,
	LocalDateTime createdAt,
	LocalDateTime modifiedAt
) {
	public static AdminAuctionResponse from(AdminAuctionInfo info) {
		return new AdminAuctionResponse(
			info.auctionId(),
			info.productName(),
			info.startPrice(),
			info.currentPrice(),
			info.endTime(),
			info.isWon(),
			info.deleted(),
			info.deletedAt(),
			info.createdAt(),
			info.modifiedAt()
		);
	}
}
