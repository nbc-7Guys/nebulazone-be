package nbc.chillguys.nebulazone.domain.bid.dto;

import java.time.LocalDateTime;

import com.querydsl.core.annotations.QueryProjection;

import nbc.chillguys.nebulazone.domain.bid.entity.BidStatus;

public record FindBidsByAuctionInfo(
	Long bidPrice,
	LocalDateTime bidTime,
	BidStatus bidStatus,
	String bidUserNickname,
	Long auctionId
) {

	@QueryProjection
	public FindBidsByAuctionInfo {
	}

}
