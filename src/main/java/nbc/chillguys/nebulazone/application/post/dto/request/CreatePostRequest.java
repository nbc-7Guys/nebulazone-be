package nbc.chillguys.nebulazone.application.post.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import nbc.chillguys.nebulazone.domain.post.entity.PostType;

public record CreatePostRequest(

	@NotBlank(message = "게시글의 제목은 필수 입력값 입니다.")
	String title,

	@NotBlank(message = "게시글 본문은 입력값 입니다.")
	String content,

	@NotNull(message = "게시글의 유형은 꼭 선택해주셔야 합니다.")
	String type) {

	public PostType getPostType() {
		return PostType.of(type);
	}
}
