package nbc.chillguys.nebulazone.application.auction.dto.response;

public record DeleteAuctionResponse(
	Long auctionId,
	Long productId
) {

	public static DeleteAuctionResponse of(Long auctionId, Long productId) {
		return new DeleteAuctionResponse(auctionId, productId);
	}
}
