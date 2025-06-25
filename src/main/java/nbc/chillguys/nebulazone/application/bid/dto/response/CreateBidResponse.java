package nbc.chillguys.nebulazone.application.bid.dto.response;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import nbc.chillguys.nebulazone.infra.redis.vo.BidVo;

public record CreateBidResponse(
	Long auctionId,
	Long bidPrice,
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	LocalDateTime bidTime
) {

	public static CreateBidResponse from(BidVo bidVo) {
		return new CreateBidResponse(
			bidVo.auctionId(),
			bidVo.bidPrice(),
			bidVo.bidCreatedAt());
	}
}
