package nbc.chillguys.nebulazone.domain.auction.dto;

import java.time.LocalDateTime;

import nbc.chillguys.nebulazone.domain.auction.entity.Auction;
import nbc.chillguys.nebulazone.domain.bid.entity.Bid;

public record ManualEndAuctionInfo(
	Long auctionId,
	Long bidId,
	Long winnerId,
	String winnerNickname,
	String winnerEmail,
	Long wonProductPrice,
	String wonProductName,
	LocalDateTime wonDate
) {

	public static ManualEndAuctionInfo from(Auction auction, Bid wonBid) {
		return new ManualEndAuctionInfo(
			auction.getId(),
			wonBid.getId(),
			wonBid.getUser().getId(),
			wonBid.getUser().getNickname(),
			wonBid.getUser().getEmail(),
			auction.getCurrentPrice(),
			auction.getProduct().getName(),
			auction.getEndTime()
		);
	}

}
