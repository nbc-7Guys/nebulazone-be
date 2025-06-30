package nbc.chillguys.nebulazone.application.post.dto.request;

import java.util.List;

import jakarta.validation.constraints.NotNull;

public record UpdateImagesPostRequest(
	@NotNull(message = "유지할 이미지 주소 목록을 입력해주세요.")
	List<String> remainImageUrls
) {
}
