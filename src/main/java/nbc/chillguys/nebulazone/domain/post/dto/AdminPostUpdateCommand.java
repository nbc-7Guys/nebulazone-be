package nbc.chillguys.nebulazone.domain.post.dto;

import java.util.List;

public record AdminPostUpdateCommand(
	Long postId,
	String title,
	String content,
	List<String> imageUrls
) {
}
