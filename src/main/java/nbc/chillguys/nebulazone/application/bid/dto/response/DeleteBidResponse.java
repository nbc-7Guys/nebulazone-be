package nbc.chillguys.nebulazone.application.bid.dto.response;

import nbc.chillguys.nebulazone.infra.redis.vo.BidVo;

public record DeleteBidResponse(
	String bidUuid,
	Long bidPrice,
	Long auctionId,
	String bidStatus
) {

	public static DeleteBidResponse from(BidVo bidVo) {
		return new DeleteBidResponse(
			bidVo.getBidUuid(),
			bidVo.getBidPrice(),
			bidVo.getAuctionId(),
			bidVo.getBidStatus()
		);
	}
}
