package nbc.chillguys.nebulazone.application.bid.dto.response;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import nbc.chillguys.nebulazone.domain.bid.dto.FindBidsByAuctionInfo;
import nbc.chillguys.nebulazone.infra.redis.vo.BidVo;

public record FindBidResponse(
	Long bidUserId,
	String bidUserNickname,
	String bidStatus,
	Long bidPrice,
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	LocalDateTime bidTime,
	Long auctionId

) {

	public static FindBidResponse from(FindBidsByAuctionInfo findBidsByAuctionInfo) {
		return new FindBidResponse(
			findBidsByAuctionInfo.bidUserId(),
			findBidsByAuctionInfo.bidUserNickname(),
			findBidsByAuctionInfo.bidStatus().name(),
			findBidsByAuctionInfo.bidPrice(),
			findBidsByAuctionInfo.bidTime(),
			findBidsByAuctionInfo.auctionId()
		);
	}

	public static FindBidResponse from(BidVo bidvo) {
		return new FindBidResponse(
			bidvo.getBidUserId(),
			bidvo.getBidUserNickname(),
			bidvo.getBidStatus(),
			bidvo.getBidPrice(),
			bidvo.getBidCreatedAt(),
			bidvo.getAuctionId());
	}
}
