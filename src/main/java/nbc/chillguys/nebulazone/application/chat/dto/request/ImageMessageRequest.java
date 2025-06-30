package nbc.chillguys.nebulazone.application.chat.dto.request;

import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ImageMessageRequest(
	@NotNull(message = "이미지 파일은 필수입니다.")
	MultipartFile image,

	@NotBlank(message = "메시지 타입은 필수입니다.")
	String type
) {
}
