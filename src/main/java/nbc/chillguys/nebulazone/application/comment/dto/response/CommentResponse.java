package nbc.chillguys.nebulazone.application.comment.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import nbc.chillguys.nebulazone.domain.comment.dto.CommentWithUserDto;

public record CommentResponse(
	Long commentId,
	Long parentId,
	String author,
	String content,
	LocalDateTime createdAt,
	LocalDateTime modifiedAt,
	List<CommentResponse> children
) {

	public static CommentResponse from(CommentWithUserDto comment) {
		return new CommentResponse(
			comment.commentId(),
			comment.parentId(),
			comment.author(),
			comment.content(),
			comment.createdAt(),
			comment.modifiedAt(),
			comment.children().stream()
				.map(CommentResponse::from)
				.toList()
		);
	}
}
