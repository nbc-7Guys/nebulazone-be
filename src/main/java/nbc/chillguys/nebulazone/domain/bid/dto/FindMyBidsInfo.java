package nbc.chillguys.nebulazone.domain.bid.dto;

import java.time.LocalDateTime;

import com.querydsl.core.annotations.QueryProjection;

import nbc.chillguys.nebulazone.domain.bid.entity.BidStatus;

public record FindMyBidsInfo(
	Long bidUserId,
	String bidUserNickname,
	BidStatus bidStatus,
	Long bidPrice,
	LocalDateTime bidTime,
	Long auctionId,
	Long productId,
	String auctionProductName
) {

	@QueryProjection
	public FindMyBidsInfo {
	}

}
