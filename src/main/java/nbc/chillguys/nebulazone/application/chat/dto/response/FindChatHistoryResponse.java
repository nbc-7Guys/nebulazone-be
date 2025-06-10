package nbc.chillguys.nebulazone.application.chat.dto.response;

import java.time.LocalDateTime;

import nbc.chillguys.nebulazone.domain.chat.entity.ChatHistory;

public record FindChatHistoryResponse(
	String senderEmail,
	String message,
	LocalDateTime sendTime
) {
	public static FindChatHistoryResponse from(ChatHistory chatHistory, String senderEmail) {
		return new FindChatHistoryResponse(
			senderEmail,
			chatHistory.getMessage(),
			chatHistory.getSendTime()
		);
	}
}
