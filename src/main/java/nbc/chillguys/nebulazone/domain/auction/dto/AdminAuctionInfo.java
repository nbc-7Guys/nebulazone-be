package nbc.chillguys.nebulazone.domain.auction.dto;

import java.time.LocalDateTime;

import nbc.chillguys.nebulazone.domain.auction.entity.Auction;

public record AdminAuctionInfo(
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
	public static AdminAuctionInfo from(Auction auction) {
		return new AdminAuctionInfo(
			auction.getId(),
			auction.getProduct().getName(),
			auction.getStartPrice(),
			auction.getCurrentPrice(),
			auction.getEndTime(),
			auction.isWon(),
			auction.isDeleted(),
			auction.getDeletedAt(),
			auction.getCreatedAt(),
			auction.getModifiedAt()
		);
	}
}
