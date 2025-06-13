package nbc.chillguys.nebulazone.application.auction.dto.response;

public record DeleteAuctionResponse(
	Long auctionId
) {

	public static DeleteAuctionResponse from(Long auctionId) {
		return new DeleteAuctionResponse(auctionId);
	}
}
