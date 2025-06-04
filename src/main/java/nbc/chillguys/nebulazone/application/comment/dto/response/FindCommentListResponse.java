package nbc.chillguys.nebulazone.application.comment.dto.response;

import java.util.List;

import org.springframework.data.domain.Page;

import nbc.chillguys.nebulazone.domain.comment.dto.CommentWithUserDto;

public record FindCommentListResponse(
	List<CommentResponse> comments,
	int curPage,
	int totalPages,
	long totalItems,
	int size
) {

	public static FindCommentListResponse of(Page<CommentWithUserDto> comments) {
		return new FindCommentListResponse(
			comments.getContent().stream()
				.map(CommentResponse::from)
				.toList(),
			comments.getNumber() + 1,
			comments.getTotalPages(),
			comments.getTotalElements(),
			comments.getSize()
		);
	}
}
