package nbc.chillguys.nebulazone.application.post.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

import nbc.chillguys.nebulazone.domain.post.entity.Post;
import nbc.chillguys.nebulazone.domain.post.entity.PostImage;
import nbc.chillguys.nebulazone.domain.post.entity.PostType;

public record UpdatePostResponse(
	String title,
	String content,
	PostType type,

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	LocalDateTime modifiedAt,

	List<String> imageUrls
) {

	public static UpdatePostResponse from(Post post) {
		return new UpdatePostResponse(
			post.getTitle(),
			post.getContent(),
			post.getType(),
			post.getModifiedAt(),
			post.getPostImages().stream()
				.map(PostImage::getUrl)
				.toList()
		);
	}
}
