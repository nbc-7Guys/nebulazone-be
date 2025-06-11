package nbc.chillguys.nebulazone.application.auction.dto.response;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import nbc.chillguys.nebulazone.domain.auction.dto.AuctionFindDetailInfo;
import nbc.chillguys.nebulazone.domain.bid.entity.Bid;

public record FindDetailAuctionResponse(
	Long auctionId,
	Long sellerId,
	String sellerNickname,
	String sellerEmail,
	Long bidUserId,
	String bidUserNickname,
	String bidUserEmail,
	Long startPrice,
	Long currentPrice,
	boolean isWon,
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	LocalDateTime endTime,
	Long productId,
	String productName,
	String productImageUrl,
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	LocalDateTime productCreatedAt,
	Long bidCount) {

	public static FindDetailAuctionResponse from(AuctionFindDetailInfo findInfo, Bid bid) {
		return new FindDetailAuctionResponse(
			findInfo.auctionId(),
			findInfo.sellerId(),
			findInfo.sellerNickname(),
			findInfo.sellerEmail(),
			bid.getUser().getId(),
			bid.getUser().getNickname(),
			bid.getUser().getEmail(),
			findInfo.startPrice(),
			findInfo.currentPrice(),
			findInfo.isWon(),
			findInfo.endTime(),
			findInfo.productId(),
			findInfo.productName(),
			findInfo.productImageUrl(),
			findInfo.productCreatedAt(),
			findInfo.bidCount()
		);
	}
}
