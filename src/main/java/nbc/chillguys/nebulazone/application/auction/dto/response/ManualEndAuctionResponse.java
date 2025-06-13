package nbc.chillguys.nebulazone.application.auction.dto.response;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import nbc.chillguys.nebulazone.domain.auction.dto.ManualEndAuctionInfo;

public record ManualEndAuctionResponse(
	Long auctionId,
	Long bidId,
	Long winnerId,
	String winnerNickname,
	String winnerEmail,
	Long wonProductPrice,
	String wonProductName,
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	LocalDateTime wonDate
) {

	public static ManualEndAuctionResponse from(ManualEndAuctionInfo auctionInfo) {
		return new ManualEndAuctionResponse(
			auctionInfo.auctionId(),
			auctionInfo.bidId(),
			auctionInfo.winnerId(),
			auctionInfo.winnerNickname(),
			auctionInfo.winnerEmail(),
			auctionInfo.wonProductPrice(),
			auctionInfo.wonProductName(),
			auctionInfo.wonDate()
		);
	}
}
