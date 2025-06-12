package nbc.chillguys.nebulazone.domain.bid.dto;

import java.time.LocalDateTime;

import nbc.chillguys.nebulazone.domain.bid.entity.Bid;

public record AdminBidInfo(
	Long bidId,
	Long auctionId,
	String auctionProductName,
	Long userId,
	String userNickname,
	Long price,
	String status,
	LocalDateTime createdAt,
	LocalDateTime modifiedAt
) {
	public static AdminBidInfo from(Bid bid) {
		return new AdminBidInfo(
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
