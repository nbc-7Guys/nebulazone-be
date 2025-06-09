package nbc.chillguys.nebulazone.application.chat.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nbc.chillguys.nebulazone.domain.auth.vo.AuthUser;
import nbc.chillguys.nebulazone.domain.chat.dto.request.ChatSendTextMessageCommand;
import nbc.chillguys.nebulazone.domain.chat.dto.response.ChatMessageInfo;
import nbc.chillguys.nebulazone.domain.chat.entity.MessageType;
import nbc.chillguys.nebulazone.domain.chat.exception.ChatErrorCode;
import nbc.chillguys.nebulazone.domain.chat.exception.ChatException;
import nbc.chillguys.nebulazone.domain.chat.service.ChatDomainService;
import nbc.chillguys.nebulazone.infra.aws.s3.S3Service;
import nbc.chillguys.nebulazone.infra.websocket.SessionUtil;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatMessageService {

	private final ChatMessageRedisService chatMessageRedisService;
	private final S3Service s3Service;
	private final SimpMessagingTemplate messagingTemplate;
	private final ChatDomainService chatDomainService;

	/**
	 * 현재 접속한 유저가 방에 참여중인지 확인
	 */
	private static AuthUser validateAuthUserInRoom(String sessionId, Long roomId) {
		AuthUser authUser = SessionUtil.getUserIdBySessionId(sessionId);
		Long roomIdBySessionId = SessionUtil.getRoomIdBySessionId(sessionId);
		if (authUser.getId() == null || roomIdBySessionId == null || !roomIdBySessionId.equals(roomId)) {
			throw new ChatException(ChatErrorCode.CHAT_ROOM_ACCESS_DENIED);
		}
		return authUser;
	}

	/**
	 * 메세지 전송(텍스트).
	 *
	 * @param sessionId 접속한 유저의 세션ID
	 * @param roomId 메시지를 보낼 방ID
	 * @param command 메시지 내용, 타입
	 */
	public void sendTextMessage(String sessionId, Long roomId, ChatSendTextMessageCommand command) {

		AuthUser authUser = validateAuthUserInRoom(sessionId, roomId);

		MessageType messageType = MessageType.valueOf(command.type());

		sendAndSaveMessage(roomId, command.message(), messageType, authUser);
	}

	/**
	 * 메세지 전송(이미지).
	 *
	 * @param authUser 접속한 유저의 인증 객체
	 * @param multipartFile 전송할 이미지 파일
	 * @param roomId 메시지를 보낼 방ID
	 * @param type 타입
	 */
	public void sendImageMessage(AuthUser authUser, MultipartFile multipartFile, Long roomId, String type) {
		chatDomainService.validateUserAccessToChatRoom(authUser, roomId);
		String imageUrl = s3Service.generateUploadUrlAndUploadFile(multipartFile);
		MessageType messageType = MessageType.valueOf(type);
		sendAndSaveMessage(roomId, imageUrl, messageType, authUser);
	}

	/**
	 * 메세지 전송 및 저장.
	 *
	 * @param roomId 메시지를 보낼 방ID
	 * @param message 전송할 메시지(url or text)
	 * @param messageType 메시지 타입
	 * @param authUser 로그인한 유저 인증 객체
	 */
	private void sendAndSaveMessage(Long roomId, String message, MessageType messageType, AuthUser authUser) {
		// 메시지 본문 작성
		ChatMessageInfo content = ChatMessageInfo.of(
			roomId,
			authUser,
			message,
			messageType,
			LocalDateTime.now()
		);

		// 메시지 전송
		messagingTemplate.convertAndSend("/topic/chat/" + roomId, content);

		// 레디스 저장
		chatMessageRedisService.saveMessageToRedis(roomId, content);
	}

	/** 채팅방 Id를 기준으로 레디스에서 채팅기록 꺼내와서 db에 저장<br/>
	 * 이벤트 형식으로 STOMP DISCONNET면 redis -> db 저장
	 *
	 * @param roomId 채팅방 id
	 */
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
