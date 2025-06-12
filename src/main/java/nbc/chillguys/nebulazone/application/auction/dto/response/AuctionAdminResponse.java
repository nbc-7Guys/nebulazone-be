package nbc.chillguys.nebulazone.application.auction.dto.response;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import nbc.chillguys.nebulazone.domain.auction.dto.AuctionAdminInfo;

public record AuctionAdminResponse(
	Long auctionId,
	String productName,
	Long startPrice,
	Long currentPrice,
	LocalDateTime endTime,
	Boolean isWon,
	Boolean deleted,
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	LocalDateTime deletedAt,
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	LocalDateTime createdAt,
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	LocalDateTime modifiedAt
) {
	public static AuctionAdminResponse from(AuctionAdminInfo info) {
		return new AuctionAdminResponse(
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
