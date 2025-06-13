package nbc.chillguys.nebulazone.application.bid.dto.response;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import nbc.chillguys.nebulazone.domain.bid.dto.BidAdminInfo;

public record BidAdminResponse(
	Long bidId,
	Long auctionId,
	String auctionProductName,
	Long userId,
	String userNickname,
	Long price,
	String status,
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	LocalDateTime createdAt,
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	LocalDateTime modifiedAt
) {
	public static BidAdminResponse from(BidAdminInfo info) {
		return new BidAdminResponse(
			info.bidId(),
			info.auctionId(),
			info.auctionProductName(),
			info.userId(),
			info.userNickname(),
			info.price(),
			info.status(),
			info.createdAt(),
			info.modifiedAt()
		);
	}
}
