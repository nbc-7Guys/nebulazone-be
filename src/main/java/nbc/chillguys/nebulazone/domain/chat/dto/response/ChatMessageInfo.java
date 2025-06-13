package nbc.chillguys.nebulazone.domain.chat.dto.response;

import java.time.LocalDateTime;

import nbc.chillguys.nebulazone.domain.auth.vo.AuthUser;
import nbc.chillguys.nebulazone.domain.chat.entity.MessageType;
import nbc.chillguys.nebulazone.infra.websocket.dto.SessionUser;

public record ChatMessageInfo(
	Long roomId,
	Long senderId,
	String senderEmail,
	String message,
	MessageType type,
	LocalDateTime sendTime
) {
	public static ChatMessageInfo of(
		Long roomId,
		SessionUser sessionUser,
		String message,
		MessageType messageType,
		LocalDateTime sendTime
	) {
		return new ChatMessageInfo(
			roomId,
			sessionUser.id(),
			sessionUser.email(),
			message,
			messageType,
			sendTime
		);
	}

	public static ChatMessageInfo of(
		Long roomId,
		Long senderId,
		String senderEmail,
		String message,
		MessageType messageType,
		LocalDateTime sendTime
	) {
		return new ChatMessageInfo(
			roomId,
			senderId,
			senderEmail,
			message,
			messageType,
			sendTime
		);
	}

}
