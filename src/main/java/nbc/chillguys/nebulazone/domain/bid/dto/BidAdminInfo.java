package nbc.chillguys.nebulazone.domain.bid.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import nbc.chillguys.nebulazone.domain.bid.entity.Bid;

public record BidAdminInfo(
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
	public static BidAdminInfo from(Bid bid) {
		return new BidAdminInfo(
			bid.getId(),
			bid.getAuction() != null ? bid.getAuction().getId() : null,
			bid.getAuction() != null ? bid.getAuction().getProduct().getName() : null,
			bid.getUser().getId(),
			bid.getUser().getNickname(),
			bid.getPrice(),
			bid.getStatus().name(),
			bid.getCreatedAt(),
			bid.getModifiedAt()
		);
	}
}
