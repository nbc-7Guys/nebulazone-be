package nbc.chillguys.nebulazone.domain.comment.dto;

import nbc.chillguys.nebulazone.domain.post.entity.Post;
import nbc.chillguys.nebulazone.domain.user.entity.User;

public record CommentDeleteCommand(
	User user,
	Post post,
	Long commentId
) {

	public static CommentDeleteCommand of(User user, Post post, Long commentId) {
		return new CommentDeleteCommand(user, post, commentId);
	}
}
