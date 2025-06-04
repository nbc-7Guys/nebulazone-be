package nbc.chillguys.nebulazone.application.post.dto.request;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import nbc.chillguys.nebulazone.domain.post.dto.PostUpdateCommand;

public record UpdatePostRequest(
	@NotBlank(message = "게시글 제목을 입력해주세요.")
	String title,

	@NotBlank(message = "게시글 본문을 입력해주세요.")
	String content,

	List<String> imageUrls
) {

	public PostUpdateCommand toCommand(Long userId, Long postId) {
		return new PostUpdateCommand(userId, postId, title, content, imageUrls);
	}
}
