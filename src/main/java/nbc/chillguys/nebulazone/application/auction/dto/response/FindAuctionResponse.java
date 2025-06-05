package nbc.chillguys.nebulazone.application.auction.dto.response;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import nbc.chillguys.nebulazone.domain.auction.dto.AuctionFindInfo;

public record FindAuctionResponse(
	Long auctionId,
	Long startPrice,
	Long currentPrice,
	boolean isClosed,
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	LocalDateTime endTime,
	String productName,
	String productImageUrl,
	Long bidCount) {

	public static FindAuctionResponse from(AuctionFindInfo findInfo) {
		return new FindAuctionResponse(
			findInfo.auctionId(),
			findInfo.startPrice(),
			findInfo.currentPrice() != null ? findInfo.currentPrice() : 0,
			findInfo.isClosed(),
			findInfo.endTime(),
			findInfo.productName(),
			findInfo.productImageUrl() != null ? findInfo.productImageUrl() : "이미지 없음",
			findInfo.bidCount()
		);
	}

}
