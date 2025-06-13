package nbc.chillguys.nebulazone.application.comment.dto.response;

public record DeleteCommentResponse(
	Long commentId
) {

	public static DeleteCommentResponse from(Long commentId) {
		return new DeleteCommentResponse(commentId);
	}
}
