package nbc.chillguys.nebulazone.application.chat.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nbc.chillguys.nebulazone.application.chat.dto.request.ImageMessageRequest;
import nbc.chillguys.nebulazone.domain.chat.dto.request.ChatSendTextMessageCommand;
import nbc.chillguys.nebulazone.domain.chat.dto.response.ChatMessageInfo;
import nbc.chillguys.nebulazone.domain.chat.entity.MessageType;
import nbc.chillguys.nebulazone.domain.chat.exception.ChatErrorCode;
import nbc.chillguys.nebulazone.domain.chat.exception.ChatException;
import nbc.chillguys.nebulazone.domain.chat.service.ChatDomainService;
import nbc.chillguys.nebulazone.domain.user.entity.User;
import nbc.chillguys.nebulazone.infra.gcs.client.GcsClient;
import nbc.chillguys.nebulazone.infra.redis.publisher.RedisMessagePublisher;
import nbc.chillguys.nebulazone.infra.redis.service.WebSocketSessionRedisService;
import nbc.chillguys.nebulazone.infra.websocket.dto.SessionUser;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatMessageService {

	private final ChatMessageRedisService chatMessageRedisService;
	private final GcsClient gcsClient;
	private final RedisMessagePublisher redisMessagePublisher;
	private final ChatDomainService chatDomainService;
	private final WebSocketSessionRedisService webSocketSessionRedisService;

	/**
	 * 현재 접속한 유저가 방에 참여중인지 확인
	 */
	private SessionUser validateSessionUserInRoom(String sessionId, Long roomId) {
		SessionUser sessionUser = webSocketSessionRedisService.getUserIdBySessionId(sessionId);
		Long roomIdBySessionId = webSocketSessionRedisService.getRoomIdBySessionId(sessionId);
		if (sessionUser.id() == null || roomIdBySessionId == null || !roomIdBySessionId.equals(roomId)) {
			throw new ChatException(ChatErrorCode.CHAT_ROOM_ACCESS_DENIED);
		}
		return sessionUser;
	}

	/**
	 * 메세지 전송(텍스트).
	 *
	 * @param sessionId 접속한 유저의 세션ID
	 * @param roomId 메시지를 보낼 방ID
	 * @param command 메시지 내용, 타입
	 */
	public void sendTextMessage(String sessionId, Long roomId, ChatSendTextMessageCommand command) {
		SessionUser sessionUser = validateSessionUserInRoom(sessionId, roomId);

		MessageType messageType = MessageType.valueOf(command.type());

		sendAndSaveMessage(roomId, command.message(), messageType, sessionUser);
	}

	/**
	 * 메세지 전송(이미지).
	 *
	 * @param user 접속한 유저의 인증 객체
	 * @param multipartFile 전송할 이미지 파일
	 * @param roomId 메시지를 보낼 방ID
	 * @param type 타입
	 */
	public void sendImageMessage(User user, Long roomId, ImageMessageRequest request) {
		SessionUser sessionUser = SessionUser.from(user);

		chatDomainService.validateUserAccessToChatRoom(user, roomId);
		String imageUrl = gcsClient.uploadFile(request.image());
		MessageType messageType = MessageType.valueOf(request.type());
		sendAndSaveMessage(roomId, imageUrl, messageType, sessionUser);
	}

	/**
	 * 메세지 전송 및 저장.
	 *
	 * @param roomId 메시지를 보낼 방ID
	 * @param message 전송할 메시지(url or text)
	 * @param messageType 메시지 타입
	 * @param sessionUser 로그인한 유저 인증 객체
	 */
	private void sendAndSaveMessage(Long roomId, String message, MessageType messageType, SessionUser sessionUser) {
		// 메시지 본문 작성
		ChatMessageInfo content = ChatMessageInfo.of(
			roomId,
			sessionUser,
			message,
			messageType,
			LocalDateTime.now()
		);

		// Redis Pub/Sub을 통해 모든 인스턴스에 메시지 발행
		redisMessagePublisher.publishChatMessage(roomId, content);

		// 레디스 저장 (채팅 기록 임시 저장)
		chatMessageRedisService.saveMessageToRedis(roomId, content);
	}

	/** 채팅방 Id를 기준으로 레디스에서 채팅기록 꺼내와서 db에 저장<br/>
	 * 이벤트 형식으로 STOMP DISCONNET면 redis -> db 저장
	 *
	 * @param roomId 채팅방 id
	 */
	@Transactional
	public void saveMessagesToDb(Long roomId) {
		// 채팅방Id를 기준으로 레디스에 있는 채팅기록들 불러오기
		List<ChatMessageInfo> messagesFromRedis = chatMessageRedisService.getMessagesFromRedis(roomId);
		if (messagesFromRedis.isEmpty()) {
			return;
		}

		// 레디스에서 가져온 메시지들 db에 저장
		chatDomainService.saveChatHistories(roomId, messagesFromRedis);

		// db에 저장이 끝난 레디스 데이터 삭제
		chatMessageRedisService.deleteMessagesInRedis(roomId);
	}

}
