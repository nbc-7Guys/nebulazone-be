package nbc.chillguys.nebulazone.application.auction.dto.request;

public record AdminAuctionSearchRequest(
	String keyword,
	Boolean deleted,
	Boolean isWon,
	int page,
	int size
) {
}
