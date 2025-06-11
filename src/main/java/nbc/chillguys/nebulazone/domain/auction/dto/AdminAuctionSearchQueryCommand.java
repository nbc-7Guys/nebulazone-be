package nbc.chillguys.nebulazone.domain.auction.dto;

public record AdminAuctionSearchQueryCommand(
	String keyword,
	Boolean deleted,
	Boolean isWon
) {
}
