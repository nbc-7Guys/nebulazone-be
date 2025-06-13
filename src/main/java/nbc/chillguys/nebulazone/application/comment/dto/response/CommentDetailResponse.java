package nbc.chillguys.nebulazone.application.comment.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

import nbc.chillguys.nebulazone.domain.comment.dto.CommentWithUserInfo;

public record CommentDetailResponse(
	Long commentId,
	Long parentId,
	String author,
	String content,

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	LocalDateTime createdAt,

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	LocalDateTime modifiedAt,

	List<CommentDetailResponse> children
) {

	public static CommentDetailResponse from(CommentWithUserInfo comment) {
		return new CommentDetailResponse(
			comment.getCommentId(),
			comment.getParentId(),
			comment.getAuthor(),
			comment.getContent(),
			comment.getCreatedAt(),
			comment.getModifiedAt(),
			comment.getChildren().stream()
				.map(CommentDetailResponse::from)
				.toList()
		);
	}
}
