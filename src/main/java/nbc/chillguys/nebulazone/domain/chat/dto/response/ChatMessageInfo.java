package nbc.chillguys.nebulazone.domain.chat.dto.response;

import java.time.LocalDateTime;

import lombok.Builder;

public record ChatMessageInfo(
	Long roomId,
	Long senderId,
	String message,
	LocalDateTime sendTime
) {
	@Builder
	public ChatMessageInfo {}
}
