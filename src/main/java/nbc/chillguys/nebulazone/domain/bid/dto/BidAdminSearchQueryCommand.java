package nbc.chillguys.nebulazone.domain.bid.dto;

import nbc.chillguys.nebulazone.domain.bid.entity.BidStatus;

public record BidAdminSearchQueryCommand(
	Long auctionId,
	Long userId,
	BidStatus status
) {
}
