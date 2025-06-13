package nbc.chillguys.nebulazone.domain.auction.dto;

public record AuctionAdminSearchQueryCommand(
	String keyword,
	Boolean deleted,
	Boolean isWon
) {
}
