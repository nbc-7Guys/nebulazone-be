package nbc.chillguys.nebulazone.domain.bid.dto;

import java.time.LocalDateTime;

import com.querydsl.core.annotations.QueryProjection;

import nbc.chillguys.nebulazone.domain.bid.entity.BidStatus;

public record FindBidInfo(
	Long bidId,
	Long bidPrice,
	LocalDateTime bidTime,
	BidStatus bidStatus,
	String nickname,
	String productName
) {

	@QueryProjection
	public FindBidInfo {
	}
}
