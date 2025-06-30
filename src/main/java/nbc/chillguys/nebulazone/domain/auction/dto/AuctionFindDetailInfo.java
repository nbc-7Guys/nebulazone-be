package nbc.chillguys.nebulazone.domain.auction.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.querydsl.core.annotations.QueryProjection;

public record AuctionFindDetailInfo(
	Long auctionId,
	Long sellerId,
	String sellerNickname,
	String sellerEmail,
	Long startPrice,
	Long currentPrice,
	boolean isWon,
	LocalDateTime endTime,
	Long productId,
	String productName,
	List<String> productImageUrl,
	LocalDateTime createdAt,
	Long bidCount
) {

	@QueryProjection
	public AuctionFindDetailInfo {
	}
}
