package nbc.chillguys.nebulazone.domain.chat.dto.response;

import java.time.LocalDateTime;

import nbc.chillguys.nebulazone.domain.auth.vo.AuthUser;
import nbc.chillguys.nebulazone.domain.chat.dto.request.ChatSendMessageCommand;
import nbc.chillguys.nebulazone.domain.chat.entity.MessageType;

public record ChatMessageInfo(
	Long roomId,
	Long senderId,
	String senderEmail,
	String message,
	String type,
	LocalDateTime sendTime
) {
	public static ChatMessageInfo of(
		Long roomId,
		AuthUser authUser,
		ChatSendMessageCommand command,
		LocalDateTime sendTime
	) {
		return new ChatMessageInfo(
			roomId,
			authUser.getId(),
			authUser.getEmail(),
			command.message(),
			// command.type(),
			"TEXT", // 이미지 업로드 추가 하면 바꿀 예정
			sendTime
		);
	}

}
