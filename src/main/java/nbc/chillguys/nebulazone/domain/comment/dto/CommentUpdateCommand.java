package nbc.chillguys.nebulazone.domain.comment.dto;

public record CommentUpdateCommand(
	Long commentId,
	Long userId,
	Long postId,
	String content
) {
}
