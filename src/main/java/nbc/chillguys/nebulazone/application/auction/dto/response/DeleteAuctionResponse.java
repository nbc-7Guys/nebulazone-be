package nbc.chillguys.nebulazone.application.auction.dto.response;

public record DeleteAuctionResponse(
	Long AuctionId
) {

	public static DeleteAuctionResponse from(Long auctionId) {
		return new DeleteAuctionResponse(auctionId);
	}
}
