package nbc.chillguys.nebulazone.application.bid.dto.response;

public record DeleteBidResponse(
	Long commentId
) {

	public static DeleteBidResponse from(Long commentId) {
		return new DeleteBidResponse(commentId);
	}
}
