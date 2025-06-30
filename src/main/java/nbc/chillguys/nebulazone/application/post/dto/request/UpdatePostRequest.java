package nbc.chillguys.nebulazone.application.post.dto.request;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import nbc.chillguys.nebulazone.domain.post.dto.PostAdminUpdateCommand;
import nbc.chillguys.nebulazone.domain.post.dto.PostUpdateCommand;

public record UpdatePostRequest(
	@NotBlank(message = "게시글 제목을 입력해주세요.")
	String title,

	@NotBlank(message = "게시글 본문을 입력해주세요.")
	String content,

	@NotNull(message = "유지할 이미지 주소 목록을 입력해주세요.")
	List<String> remainImageUrls
) {

	public PostUpdateCommand toCommand(List<String> imageUrls) {
		return new PostUpdateCommand(title, content, imageUrls);
	}

	public PostAdminUpdateCommand toAdminCommand(Long postId, List<String> imageUrls) {
		return new PostAdminUpdateCommand(postId, title, content, imageUrls);
	}
}
