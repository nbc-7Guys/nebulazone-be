package nbc.chillguys.nebulazone.application.chat.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nbc.chillguys.nebulazone.domain.auth.vo.AuthUser;
import nbc.chillguys.nebulazone.domain.chat.dto.request.ChatSendMessageCommand;
import nbc.chillguys.nebulazone.domain.chat.dto.response.ChatMessageInfo;
import nbc.chillguys.nebulazone.domain.chat.exception.ChatErrorCode;
import nbc.chillguys.nebulazone.domain.chat.exception.ChatException;
import nbc.chillguys.nebulazone.domain.chat.service.ChatDomainService;
import nbc.chillguys.nebulazone.infra.websocket.SessionUtil;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatMessageService {

	private final ChatMessageRedisService chatMessageRedisService;
	private final SimpMessagingTemplate messagingTemplate;
	private final ChatDomainService chatDomainService;

	/**
	 * 메세지 전송.
	 *
	 * @param roomId the room id
	 * @param command the command
	 */
	@Transactional
	public void sendMessage(String sessionId, Long roomId, ChatSendMessageCommand command) {

		AuthUser authUser = SessionUtil.getUserIdBySessionId(sessionId);
		Long roomIdBySessionId = SessionUtil.getRoomIdBySessionId(sessionId);

		if (authUser.getId() == null || roomIdBySessionId == null || !roomIdBySessionId.equals(roomId)) {
			throw new ChatException(ChatErrorCode.CHAT_ROOM_ACCESS_DENIED);
		}

		// 메시지 본문 작성
		ChatMessageInfo message = ChatMessageInfo.of(
			roomId,
			authUser,
			command,
			LocalDateTime.now()
		);

		// 메시지 전송
		messagingTemplate.convertAndSend("/topic/chat/" + roomId, message);

		// 레디스 저장
		chatMessageRedisService.saveMessageToRedis(roomId, message);
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
