package nbc.chillguys.nebulazone.domain.auction.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import nbc.chillguys.nebulazone.domain.auction.entity.Auction;

public record AuctionAdminInfo(
	Long auctionId,
	String productName,
	Long startPrice,
	Long currentPrice,
	LocalDateTime endTime,
	Boolean isWon,
	Boolean deleted,
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	LocalDateTime deletedAt,
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	LocalDateTime createdAt,
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	LocalDateTime modifiedAt
) {
	public static AuctionAdminInfo from(Auction auction) {
		return new AuctionAdminInfo(
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
