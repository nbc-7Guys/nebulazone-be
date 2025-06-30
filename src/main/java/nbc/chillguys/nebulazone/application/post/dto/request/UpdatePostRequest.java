package nbc.chillguys.nebulazone.application.post.dto.request;

import jakarta.validation.constraints.NotBlank;
import nbc.chillguys.nebulazone.domain.post.dto.PostAdminUpdateCommand;
import nbc.chillguys.nebulazone.domain.post.dto.PostUpdateCommand;

public record UpdatePostRequest(
	@NotBlank(message = "게시글 제목을 입력해주세요.")
	String title,

	@NotBlank(message = "게시글 본문을 입력해주세요.")
	String content
) {

	public PostUpdateCommand toCommand() {
		return new PostUpdateCommand(title, content);
	}

	public PostAdminUpdateCommand toAdminCommand(Long postId) {
		return new PostAdminUpdateCommand(postId, title, content);
	}
}
