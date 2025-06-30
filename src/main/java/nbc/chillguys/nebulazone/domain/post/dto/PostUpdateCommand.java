package nbc.chillguys.nebulazone.domain.post.dto;

import java.util.List;

public record PostUpdateCommand(
	String title,
	String content,
	List<String> imageUrls
) {
}
