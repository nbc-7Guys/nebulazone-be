package nbc.chillguys.nebulazone.application.auction.dto.response;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import nbc.chillguys.nebulazone.domain.auction.dto.AuctionFindAllInfo;

public record FindAllAuctionResponse(
	Long auctionId,
	Long startPrice,
	Long currentPrice,
	boolean isWon,
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	LocalDateTime endTime,
	String productName,
	String productImageUrl,
	Long bidCount) {

	public static FindAllAuctionResponse from(AuctionFindAllInfo findInfo) {
		return new FindAllAuctionResponse(
			findInfo.auctionId(),
			findInfo.startPrice(),
			findInfo.currentPrice(),
			findInfo.isWon(),
			findInfo.endTime(),
			findInfo.productName(),
			findInfo.productImageUrl() != null ? findInfo.productImageUrl() : "이미지 없음",
			findInfo.bidCount()
		);
	}

}
