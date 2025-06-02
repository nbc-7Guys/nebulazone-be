package nbc.chillguys.nebulazone.application.post.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Builder;
import nbc.chillguys.nebulazone.domain.post.entity.Post;
import nbc.chillguys.nebulazone.domain.post.entity.PostType;

@Builder
public record CreatePostResponse(

	String title, String content, PostType type, LocalDateTime modifiedAt, List<String> imageUrls) {

	public static CreatePostResponse from(Post post, List<String> imageUrls) {
		return CreatePostResponse.builder()
			.title(post.getTitle())
			.content(post.getContent())
			.type(post.getType())
			.modifiedAt(post.getModifiedAt())
			.imageUrls(imageUrls)
			.build();

	}
}
