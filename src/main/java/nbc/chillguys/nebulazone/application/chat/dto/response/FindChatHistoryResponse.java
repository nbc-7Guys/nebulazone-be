package nbc.chillguys.nebulazone.application.chat.dto.response;

import java.time.LocalDateTime;

import lombok.Builder;

@Builder
public record FindChatHistoryResponse(
	String sender,
	String message,
	LocalDateTime sendTime
) {
}
