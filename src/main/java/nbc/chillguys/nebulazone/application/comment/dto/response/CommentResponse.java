package nbc.chillguys.nebulazone.application.comment.dto.response;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import nbc.chillguys.nebulazone.domain.comment.entity.Comment;

public record CommentResponse(
	Long commentId,
	Long postId,
	Long parentId,
	String content,

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	LocalDateTime createdAt,

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	LocalDateTime modifiedAt
) {

	public static CommentResponse from(Comment comment) {
		return new CommentResponse(
			comment.getId(),
			comment.getPost().getId(),
			comment.getParent() != null ? comment.getParent().getId() : null,
			comment.getContent(),
			comment.getCreatedAt(),
			comment.getModifiedAt()
		);
	}
}
