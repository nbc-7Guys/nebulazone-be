package nbc.chillguys.nebulazone.domain.bid.dto;

import nbc.chillguys.nebulazone.domain.bid.entity.BidStatus;

public record AdminBidSearchQueryCommand(
	Long auctionId,
	Long userId,
	BidStatus status
) {
}
