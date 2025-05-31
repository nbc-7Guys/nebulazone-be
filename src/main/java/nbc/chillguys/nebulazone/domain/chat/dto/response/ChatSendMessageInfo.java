package nbc.chillguys.nebulazone.domain.chat.dto.response;

public record ChatSendMessageInfo(
	Long senderId,
	String message
) {
}
