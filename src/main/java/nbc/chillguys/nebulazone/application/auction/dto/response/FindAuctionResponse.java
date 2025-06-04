package nbc.chillguys.nebulazone.application.auction.dto.response;

import java.time.LocalDateTime;

import lombok.Builder;
import nbc.chillguys.nebulazone.domain.auction.dto.AuctionFindInfo;

@Builder
public record FindAuctionResponse(
	Long auctionId,
	Long startPrice,
	Long currentPrice,
	boolean isClosed,
	LocalDateTime endTime,
	String productName,
	String productImageUrl,
	Long bidCount) {

	public static FindAuctionResponse from(AuctionFindInfo findInfo) {
		return FindAuctionResponse.builder()
			.auctionId(findInfo.auctionId())
			.startPrice(findInfo.startPrice())
			.currentPrice(findInfo.currentPrice() != null ? findInfo.currentPrice() : 0)
			.isClosed(findInfo.isClosed())
			.endTime(findInfo.endTime())
			.productName(findInfo.productName())
			.productImageUrl(findInfo.productImageUrl() != null ? findInfo.productImageUrl() : "이미지 없음")
			.bidCount(findInfo.bidCount())
			.build();
	}

}
