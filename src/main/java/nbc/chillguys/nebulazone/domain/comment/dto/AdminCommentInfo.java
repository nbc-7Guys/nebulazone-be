package nbc.chillguys.nebulazone.domain.comment.dto;

import java.time.LocalDateTime;

import nbc.chillguys.nebulazone.domain.comment.entity.Comment;

public record AdminCommentInfo(
	Long commentId,
	String content,
	String writer,
	Long postId,
	String postTitle,
	Long parentId,
	Boolean deleted,
	LocalDateTime deletedAt,
	LocalDateTime createdAt,
	LocalDateTime modifiedAt
) {
	public static AdminCommentInfo from(Comment comment) {
		return new AdminCommentInfo(
			comment.getId(),
			comment.getContent(),
			comment.getUser().getNickname(),
			comment.getPost().getId(),
			comment.getPost().getTitle(),
			comment.getParent() != null ? comment.getParent().getId() : null,
			comment.isDeleted(),
			comment.getDeletedAt(),
			comment.getCreatedAt(),
			comment.getModifiedAt()
		);
	}
}
