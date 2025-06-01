package nbc.chillguys.nebulazone.application.chat.dto.request;

import lombok.Builder;

@Builder
public record FindChatHistoryRequest (
	String chatRoomId
) {
}
