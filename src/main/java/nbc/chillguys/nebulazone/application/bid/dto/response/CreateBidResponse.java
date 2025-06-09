package nbc.chillguys.nebulazone.application.bid.dto.response;

import nbc.chillguys.nebulazone.domain.bid.entity.Bid;

public record CreateBidResponse(
	Long bidId,
	Long bidPrice
) {

	public static CreateBidResponse from(Bid bid) {
		return new CreateBidResponse(bid.getId(), bid.getPrice());
	}
}
