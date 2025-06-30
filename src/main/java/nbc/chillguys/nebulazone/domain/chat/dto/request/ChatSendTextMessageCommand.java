package nbc.chillguys.nebulazone.domain.chat.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ChatSendTextMessageCommand(
	@NotBlank(message = "message를 입력해 주세요")
	String message,

	@Pattern(regexp = "TEXT|IMAGE", message = "type은 'TEXT' 또는 'IMAGE'만 가능합니다.")
	String type
) {
}
