package nbc.chillguys.nebulazone.application.comment.dto.request;

import jakarta.validation.constraints.NotBlank;
import nbc.chillguys.nebulazone.domain.comment.dto.CommentUpdateCommand;

public record UpdateCommentRequest(
	@NotBlank(message = "게시글 본문을 입력해주세요.")
	String content
) {

	public CommentUpdateCommand toCommand(Long commentId, Long userId, Long postId) {
		return new CommentUpdateCommand(commentId, userId, postId, content);
	}
}
