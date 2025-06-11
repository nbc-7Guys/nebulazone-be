package nbc.chillguys.nebulazone.domain.auction.dto;

import java.time.LocalDateTime;

import com.querydsl.core.annotations.QueryProjection;

public record AuctionFindAllInfo(
	Long auctionId,
	Long startPrice,
	Long currentPrice,
	boolean isWon,
	LocalDateTime endTime,
	LocalDateTime createdAt,
	String productName,
	String productImageUrl,
	Long bidCount
) {

	@QueryProjection
	public AuctionFindAllInfo {
	}

}
