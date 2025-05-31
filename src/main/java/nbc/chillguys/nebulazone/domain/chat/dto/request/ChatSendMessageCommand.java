package nbc.chillguys.nebulazone.domain.chat.dto.request;

import lombok.Builder;

public record ChatSendMessageCommand(
	String message,
	String type
) {
	@Builder
	public ChatSendMessageCommand {}
}
