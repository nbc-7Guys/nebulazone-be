package nbc.chillguys.nebulazone.domain.comment.dto;

public record CommentDeleteCommand(
	Long commentId,
	Long userId,
	Long postId
) {

	public static CommentDeleteCommand of(Long commentId, Long userId, Long postId) {
		return new CommentDeleteCommand(commentId, userId, postId);
	}
}
