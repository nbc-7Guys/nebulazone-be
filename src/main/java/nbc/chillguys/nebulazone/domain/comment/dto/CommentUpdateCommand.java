package nbc.chillguys.nebulazone.domain.comment.dto;

import nbc.chillguys.nebulazone.domain.post.entity.Post;
import nbc.chillguys.nebulazone.domain.user.entity.User;

public record CommentUpdateCommand(
	User user,
	Post post,
	Long commentId,
	String content
) {
}
