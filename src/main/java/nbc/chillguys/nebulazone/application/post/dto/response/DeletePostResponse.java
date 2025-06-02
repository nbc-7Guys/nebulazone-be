package nbc.chillguys.nebulazone.application.post.dto.response;

public record DeletePostResponse(
	Long postId
) {

	public static DeletePostResponse from(Long postId) {
		return new DeletePostResponse(postId);
	}
}
