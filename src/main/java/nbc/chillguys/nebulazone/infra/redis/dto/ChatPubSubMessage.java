package nbc.chillguys.nebulazone.infra.redis.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import nbc.chillguys.nebulazone.domain.chat.dto.response.ChatMessageInfo;
import nbc.chillguys.nebulazone.domain.chat.entity.MessageType;

/**
 * Redis Pub/Sub으로 전송할 채팅 메시지 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatPubSubMessage {

	private Long roomId;
	private Long senderId;
	private String senderEmail;
	private String message;
	private MessageType messageType;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private LocalDateTime timestamp;

	/**
	 * ChatMessageInfo를 ChatPubSubMessage로 변환
	 */
	public static ChatPubSubMessage from(Long roomId, ChatMessageInfo chatMessageInfo) {
		return ChatPubSubMessage.builder()
			.roomId(roomId)
			.senderId(chatMessageInfo.senderId())
			.senderEmail(chatMessageInfo.senderEmail())
			.message(chatMessageInfo.message())
			.messageType(chatMessageInfo.type())
			.timestamp(chatMessageInfo.sendTime())
			.build();
	}

	/**
	 * Redis 채널명 생성
	 */
	public static String getChannelName(Long roomId) {
		return "chat:room:" + roomId;
	}

	/**
	 * ChatPubSubMessage를 ChatMessageInfo로 변환
	 */
	public ChatMessageInfo toChatMessageInfo() {
		return ChatMessageInfo.of(
			this.roomId,
			this.senderId,
			this.senderEmail,
			this.message,
			this.messageType,
			this.timestamp
		);
	}
}
