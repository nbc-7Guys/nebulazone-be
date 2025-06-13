package nbc.chillguys.nebulazone.domain.post.dto;

import java.util.List;

public record PostAdminUpdateCommand(
	Long postId,
	String title,
	String content,
	List<String> imageUrls
) {
}
