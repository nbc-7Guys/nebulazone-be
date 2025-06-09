package nbc.chillguys.nebulazone.application.post.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

import nbc.chillguys.nebulazone.domain.post.entity.Post;
import nbc.chillguys.nebulazone.domain.post.entity.PostImage;

public record GetPostResponse(
	Long postId,
	String title,
	String content,
	String type,
	Long userId,
	String author,
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	LocalDateTime createdAt,
	List<String> imageUrls
) {
	public static GetPostResponse from(Post post) {
		return new GetPostResponse(
			post.getId(),
			post.getTitle(),
			post.getContent(),
			post.getType().name(),
			post.getUserId(),
			post.getUserNickname(),
			post.getCreatedAt(),
			post.getPostImages().stream()
				.map(PostImage::getUrl)
				.toList()
		);
	}
}
