package nbc.chillguys.nebulazone.application.comment.dto.request;

import jakarta.validation.constraints.NotBlank;
import nbc.chillguys.nebulazone.domain.comment.dto.CommentUpdateCommand;
import nbc.chillguys.nebulazone.domain.post.entity.Post;
import nbc.chillguys.nebulazone.domain.user.entity.User;

public record UpdateCommentRequest(
	@NotBlank(message = "게시글 본문을 입력해주세요.")
	String content
) {

	public CommentUpdateCommand toCommand(User user, Post post, Long commentId) {
		return new CommentUpdateCommand(user, post, commentId, content);
	}
}
