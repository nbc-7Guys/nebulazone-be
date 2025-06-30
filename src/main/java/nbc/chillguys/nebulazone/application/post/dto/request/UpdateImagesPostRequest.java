package nbc.chillguys.nebulazone.application.post.dto.request;

import java.util.List;

public record UpdateImagesPostRequest(
	List<String> remainImageUrls
) {
}
