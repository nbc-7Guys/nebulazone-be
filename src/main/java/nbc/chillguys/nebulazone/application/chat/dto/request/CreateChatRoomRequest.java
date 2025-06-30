package nbc.chillguys.nebulazone.application.chat.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CreateChatRoomRequest(
	Long productId
) {
}
