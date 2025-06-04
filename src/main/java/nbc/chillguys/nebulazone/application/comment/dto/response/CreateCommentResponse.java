package nbc.chillguys.nebulazone.application.comment.dto.response;

import java.time.LocalDateTime;

import nbc.chillguys.nebulazone.domain.comment.entity.Comment;

public record CreateCommentResponse(
	Long commentId,
	Long postId,
	Long parentId,
	String content,
	LocalDateTime createdAt,
	LocalDateTime modifiedAt
) {

	public static CreateCommentResponse from(Comment comment) {
		return new CreateCommentResponse(
			comment.getId(),
			comment.getPost().getId(),
			comment.getParent() != null ? comment.getParent().getId() : null,
			comment.getContent(),
			comment.getCreatedAt(),
			comment.getModifiedAt()
		);
	}
}
