package nbc.chillguys.nebulazone.domain.chat.dto.response;

import java.time.LocalDateTime;

import lombok.Builder;

@Builder
public record ChatMessageInfo(
	Long roomId,
	Long senderId,
	String senderNickname,
	String message,
	LocalDateTime sendTime
) {
}
