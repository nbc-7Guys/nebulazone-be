package nbc.chillguys.nebulazone.domain.post.dto;

import lombok.Builder;
import nbc.chillguys.nebulazone.application.post.dto.request.CreatePostRequest;
import nbc.chillguys.nebulazone.domain.post.entity.PostType;
import nbc.chillguys.nebulazone.domain.user.entity.User;

@Builder
public record PostCreateCommand(
	User user,
	String title,
	String content,
	PostType type
) {

	public static PostCreateCommand of(User user, CreatePostRequest request) {
		return PostCreateCommand.builder()
			.user(user)
			.title(request.title())
			.content(request.content())
			.type(request.getPostType())
			.build();
	}

}
