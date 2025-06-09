package nbc.chillguys.nebulazone.domain.chat.dto.request;

public record ChatSendMessageCommand(
	String message,
	String type
) {
}
