package nbc.chillguys.nebulazone.domain.auction.dto;

import java.time.LocalDateTime;

import nbc.chillguys.nebulazone.domain.auction.entity.Auction;
import nbc.chillguys.nebulazone.domain.bid.entity.Bid;
import nbc.chillguys.nebulazone.domain.user.entity.User;

public record ManualEndAuctionInfo(
	Long auctionId,
	Long BidId,
	Long winnerId,
	String winnerNickname,
	String winnerEmail,
	Long wonProductPrice,
	String wonProductName,
	LocalDateTime wonDate
) {

	public static ManualEndAuctionInfo from(Auction auction, Bid wonBid, User user) {
		return new ManualEndAuctionInfo(
			auction.getId(),
			wonBid.getId(),
			user.getId(),
			user.getNickname(),
			user.getEmail(),
			auction.getCurrentPrice(),
			auction.getProduct().getName(),
			auction.getEndTime()
		);
	}

}
