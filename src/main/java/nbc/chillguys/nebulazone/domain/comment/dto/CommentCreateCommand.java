package nbc.chillguys.nebulazone.domain.comment.dto;

import nbc.chillguys.nebulazone.domain.post.entity.Post;
import nbc.chillguys.nebulazone.domain.user.entity.User;

public record CommentCreateCommand(
	User user,
	Post post,
	String content,
	Long parentId
) {
}
