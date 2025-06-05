package nbc.chillguys.nebulazone.application.bid.dto.response;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import nbc.chillguys.nebulazone.domain.bid.dto.FindBidInfo;

public record FindBidResponse(
	Long BidId,
	String nickname,
	String productName,
	String bidStatusMessage,
	Long bidPrice,
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	LocalDateTime bidTime

) {

	public static FindBidResponse from(FindBidInfo findBidInfo) {
		return new FindBidResponse(
			findBidInfo.bidId(),
			findBidInfo.nickname(),
			findBidInfo.productName(),
			findBidInfo.bidStatus().getMessage(),
			findBidInfo.bidPrice(),
			findBidInfo.bidTime());
	}
}
