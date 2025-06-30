package nbc.chillguys.nebulazone.application.post.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

import nbc.chillguys.nebulazone.domain.post.vo.PostDocument;

public record SearchPostResponse(
	Long postId,
	String title,
	String content,
	String type,
	Long userId,
	String author,
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	LocalDateTime modifiedAt,
	List<String> imageUrls
) {
	public static SearchPostResponse from(PostDocument postDocument) {
		return new SearchPostResponse(
			postDocument.postId(),
			postDocument.title(),
			postDocument.content(),
			postDocument.type(),
			postDocument.userId(),
			postDocument.author(),
			postDocument.modifiedAt(),
			postDocument.imageUrls()
		);
	}
}
