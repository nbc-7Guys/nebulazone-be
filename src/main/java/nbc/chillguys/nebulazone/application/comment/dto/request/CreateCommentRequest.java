package nbc.chillguys.nebulazone.application.comment.dto.request;

import jakarta.validation.constraints.NotBlank;
import nbc.chillguys.nebulazone.domain.comment.dto.CommentCreateCommand;
import nbc.chillguys.nebulazone.domain.post.entity.Post;
import nbc.chillguys.nebulazone.domain.user.entity.User;

public record CreateCommentRequest(
	@NotBlank(message = "게시글 본문을 입력해주세요.")
	String content,

	Long parentId
) {

	public CommentCreateCommand toCommand(User user, Post post) {
		return new CommentCreateCommand(user, post, content, parentId == null ? -1 : parentId);
	}
}
