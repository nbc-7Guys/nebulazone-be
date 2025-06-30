package nbc.chillguys.nebulazone.application.chat.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CreateChatRoomRequest(
	@NotBlank(message = "ProductId를 입력해 주세요")
	Long productId
) {
}
