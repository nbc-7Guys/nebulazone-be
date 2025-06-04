package nbc.chillguys.nebulazone.application.chat.service;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nbc.chillguys.nebulazone.application.user.service.UserService;
import nbc.chillguys.nebulazone.domain.auth.vo.AuthUser;
import nbc.chillguys.nebulazone.domain.chat.dto.request.ChatSendMessageCommand;
import nbc.chillguys.nebulazone.domain.chat.dto.response.ChatMessageInfo;
import nbc.chillguys.nebulazone.domain.chat.entity.ChatHistory;
import nbc.chillguys.nebulazone.domain.chat.entity.ChatRoom;
import nbc.chillguys.nebulazone.domain.chat.exception.ChatErrorCode;
import nbc.chillguys.nebulazone.domain.chat.exception.ChatException;
import nbc.chillguys.nebulazone.domain.chat.repository.ChatRoomHistoryRepository;
import nbc.chillguys.nebulazone.domain.chat.repository.ChatRoomRepository;
import nbc.chillguys.nebulazone.domain.chat.repository.ChatRoomUserRepository;
import nbc.chillguys.nebulazone.domain.chat.service.ChatDomainService;
import nbc.chillguys.nebulazone.domain.user.entity.User;
import nbc.chillguys.nebulazone.domain.user.exception.UserErrorCode;
import nbc.chillguys.nebulazone.domain.user.exception.UserException;
import nbc.chillguys.nebulazone.domain.user.repository.UserRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatMessageService {

	private final ChatRoomRepository chatRoomRepository;
	private final ChatRoomUserRepository chatRoomUserRepository;
	private final UserRepository userRepository;
	private final ChatDomainService chatDomainService;
	private final ChatMessageRedisService chatMessageRedisService;
	private final SimpMessagingTemplate messagingTemplate;
	private final ChatRoomHistoryRepository chatRoomHistoryRepository;


	/**
	 * 메세지 전송.
	 *
	 * @param roomId the room id
	 * @param command the command
	 */
	@Transactional
	public void sendMessage(Long userId, Long roomId, ChatSendMessageCommand command) {

		// 채팅방 존재 확인
		boolean existsed = chatRoomRepository.existsChatRoomById(roomId);
		if (!existsed) {
			throw new ChatException(ChatErrorCode.CHAT_ROOM_NOT_FOUND);
		}

		// 참가자 확인
		boolean isParticipant = chatRoomUserRepository.existsByIdChatRoomIdAndIdUserId(roomId, userId);
		if (!isParticipant) {
			throw new ChatException(ChatErrorCode.CHAT_ROOM_ACCESS_DENIED);
		}

		// 발신자 정보 조회
		User sender = userRepository.findById(userId)
			.orElseThrow(() -> new IllegalArgumentException("유저 정보를 찾을 수 없습니다."));

		// 메시지 본문 작성
		ChatMessageInfo message = chatDomainService.createMessage(roomId, sender, command.message(), command.type());

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
		if (messagesFromRedis.isEmpty())
			return;

		ChatRoom chatRoom = chatRoomRepository.findById(roomId)
			.orElseThrow(() -> new ChatException(ChatErrorCode.CHAT_ROOM_NOT_FOUND));

		List<ChatHistory> histories = new ArrayList<>();

		// 레디스에서 불러온 채팅기록들을 for문을 돌면서 채팅기록 테이블에 저장할 리스트에 추가
		for (ChatMessageInfo messages : messagesFromRedis) {

			ChatHistory history = ChatHistory.builder().chatRoom(chatRoom).userId(1L) // N + 1 문제 발생
				.message(messages.message()).sendtime(messages.sendTime()).build();

			histories.add(history);
		}

		chatRoomHistoryRepository.saveAll(histories);

		// db에 저장이 끝난 레디스 데이터 삭제
		chatMessageRedisService.deleteMessagesInRedis(roomId);
	}

}
