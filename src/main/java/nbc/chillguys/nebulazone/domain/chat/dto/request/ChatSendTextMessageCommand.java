package nbc.chillguys.nebulazone.domain.chat.dto.request;

public record ChatSendTextMessageCommand(
	String message,
	String type
) {
}
