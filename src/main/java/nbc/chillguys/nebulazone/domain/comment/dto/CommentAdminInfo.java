package nbc.chillguys.nebulazone.domain.comment.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import nbc.chillguys.nebulazone.domain.comment.entity.Comment;

public record CommentAdminInfo(
	Long commentId,
	String content,
	String writer,
	Long postId,
	String postTitle,
	Long parentId,
	Boolean deleted,
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	LocalDateTime deletedAt,
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	LocalDateTime createdAt,
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	LocalDateTime modifiedAt
) {
	public static CommentAdminInfo from(Comment comment) {
		return new CommentAdminInfo(
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
