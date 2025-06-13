package nbc.chillguys.nebulazone.application.auction.dto.request;

public record AuctionAdminSearchRequest(
	String keyword,
	Boolean deleted,
	Boolean isWon,
	int page,
	int size
) {
}
