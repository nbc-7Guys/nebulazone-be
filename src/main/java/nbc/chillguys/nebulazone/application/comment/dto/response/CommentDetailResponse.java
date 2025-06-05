package nbc.chillguys.nebulazone.application.comment.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import nbc.chillguys.nebulazone.domain.comment.dto.CommentWithUserDto;

public record CommentDetailResponse(
	Long commentId,
	Long parentId,
	String author,
	String content,
	LocalDateTime createdAt,
	LocalDateTime modifiedAt,
	List<CommentDetailResponse> children
) {

	public static CommentDetailResponse from(CommentWithUserDto comment) {
		return new CommentDetailResponse(
			comment.commentId(),
			comment.parentId(),
			comment.author(),
			comment.content(),
			comment.createdAt(),
			comment.modifiedAt(),
			comment.children().stream()
				.map(CommentDetailResponse::from)
				.toList()
		);
	}
}
