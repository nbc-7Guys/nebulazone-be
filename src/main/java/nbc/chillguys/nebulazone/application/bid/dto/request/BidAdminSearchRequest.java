package nbc.chillguys.nebulazone.application.bid.dto.request;

import nbc.chillguys.nebulazone.domain.bid.entity.BidStatus;

public record BidAdminSearchRequest(
	Long auctionId,
	Long userId,
	BidStatus status,
	int page,
	int size
) {
}
