package nbc.chillguys.nebulazone.application.chat.dto.response;

import java.time.LocalDateTime;

import nbc.chillguys.nebulazone.domain.chat.entity.ChatHistory;

public record FindChatHistoryResponse(
	Long senderId,
	String message,
	LocalDateTime sendTime
) {
	public static FindChatHistoryResponse from(ChatHistory chatHistory) {
		return new FindChatHistoryResponse(
			chatHistory.getUserId(),
			chatHistory.getMessage(),
			chatHistory.getSendTime()
		);
	}
}
