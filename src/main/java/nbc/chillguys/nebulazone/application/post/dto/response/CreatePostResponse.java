package nbc.chillguys.nebulazone.application.post.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

import nbc.chillguys.nebulazone.domain.post.entity.Post;
import nbc.chillguys.nebulazone.domain.post.entity.PostType;

public record CreatePostResponse(
	Long postId,
	String title,
	String content,
	PostType type,
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	LocalDateTime modifiedAt,
	List<String> imageUrls) {

	public static CreatePostResponse from(Post post, List<String> imageUrls) {
		return new CreatePostResponse(
			post.getId(),
			post.getTitle(),
			post.getContent(),
			post.getType(),
			post.getModifiedAt(),
			imageUrls
		);
	}
}
