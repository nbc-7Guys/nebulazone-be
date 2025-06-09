package nbc.chillguys.nebulazone.application.chat.dto.response;

import java.time.LocalDateTime;

import nbc.chillguys.nebulazone.domain.chat.entity.ChatHistory;

public record FindChatHistoryResponse(
	String sender,
	String message,
	LocalDateTime sendTime
) {
	public static FindChatHistoryResponse from(final ChatHistory chatHistory, String senderNickname) {
		return new FindChatHistoryResponse(
			senderNickname,
			chatHistory.getMessage(),
			chatHistory.getSendTime()
		);
	}
}
