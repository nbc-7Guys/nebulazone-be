package nbc.chillguys.nebulazone.application.post.dto.response;

import java.time.LocalDateTime;

import lombok.Builder;
import nbc.chillguys.nebulazone.domain.post.entity.Post;
import nbc.chillguys.nebulazone.domain.post.entity.PostType;

@Builder
public record CreatePostResponse(

	String title, String content, PostType type, LocalDateTime modifiedAt) {

	public static CreatePostResponse from(Post post) {
		return CreatePostResponse.builder()
			.title(post.getTitle())
			.content(post.getContent())
			.type(post.getType())
			.modifiedAt(post.getModifiedAt())
			.build();

	}
}
