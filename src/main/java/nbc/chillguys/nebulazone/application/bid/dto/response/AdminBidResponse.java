package nbc.chillguys.nebulazone.application.bid.dto.response;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import nbc.chillguys.nebulazone.domain.bid.dto.AdminBidInfo;

public record AdminBidResponse(
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
	public static AdminBidResponse from(AdminBidInfo info) {
		return new AdminBidResponse(
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
