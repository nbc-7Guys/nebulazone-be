package nbc.chillguys.nebulazone.domain.post.dto;

import nbc.chillguys.nebulazone.application.post.dto.request.CreatePostRequest;
import nbc.chillguys.nebulazone.domain.post.entity.PostType;
import nbc.chillguys.nebulazone.domain.user.entity.User;

public record PostCreateCommand(
	User user,
	String title,
	String content,
	PostType type
) {

	public static PostCreateCommand of(User user, CreatePostRequest request) {
		return new PostCreateCommand(
			user,
			request.title(),
			request.content(),
			request.getPostType()
		);
	}

}
