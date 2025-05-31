package nbc.chillguys.nebulazone.application.chat.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.application.chat.dto.request.CreateChatRoomRequest;
import nbc.chillguys.nebulazone.domain.auth.vo.AuthUser;
import nbc.chillguys.nebulazone.domain.chat.dto.response.ChatRoomCreationInfo;
import nbc.chillguys.nebulazone.domain.chat.dto.request.ChatSendMessageCommand;
import nbc.chillguys.nebulazone.domain.chat.dto.response.ChatMessageInfo;
import nbc.chillguys.nebulazone.application.chat.dto.response.CreateChatRoomResponse;
import nbc.chillguys.nebulazone.domain.chat.entity.ChatHistory;
import nbc.chillguys.nebulazone.domain.chat.entity.ChatRoom;
import nbc.chillguys.nebulazone.domain.chat.exception.ChatErrorCode;
import nbc.chillguys.nebulazone.domain.chat.exception.ChatException;
import nbc.chillguys.nebulazone.domain.chat.repository.ChatRoomHistoryRepository;
import nbc.chillguys.nebulazone.domain.chat.repository.ChatRoomRepository;
import nbc.chillguys.nebulazone.domain.chat.repository.ChatRoomUserRepository;
import nbc.chillguys.nebulazone.domain.chat.service.ChatDomainService;
import nbc.chillguys.nebulazone.domain.products.entity.Product;
import nbc.chillguys.nebulazone.domain.products.repository.ProductRepository;
import nbc.chillguys.nebulazone.domain.user.entity.User;
import nbc.chillguys.nebulazone.domain.user.exception.UserErrorCode;
import nbc.chillguys.nebulazone.domain.user.exception.UserException;
import nbc.chillguys.nebulazone.domain.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class ChatService {

	private final ChatRoomRepository chatRoomRepository;
	private final ProductRepository productRepository;
	private final UserRepository userRepository;
	private final ChatRoomUserRepository chatRoomUserRepository;
	private final ChatMessageRedisService chatMessageRedisService;
	private final SimpMessagingTemplate messagingTemplate;
	private final ChatDomainService chatDomainService;
	private final ChatRoomHistoryRepository chatRoomHistoryRepository;

	/**
	 * 채팅방 생성 또는 기존에 채팅방 조회.<br/>
	 * 채팅방이 없다면 생성하고 있다면 기존 채팅방 정보를 가져온다.
	 *
	 * @param authUser the auth user
	 * @param request the request
	 * @return the chat room info
	 */
	@Transactional
	public CreateChatRoomResponse createOrGet(AuthUser authUser, CreateChatRoomRequest request) {

		// 기존에 참가했던 채팅방이 있는지 확인
		Optional<ChatRoom> room = chatRoomRepository.findChatRoom(request.productId(), authUser.getId(),
			request.sellerId());
		if (room.isPresent()) {
			return CreateChatRoomResponse.of(room.get());
		}

		Product product = productRepository.findById(request.productId())
			.orElseThrow(() -> new RuntimeException("상품을 찾을 수 없습니다."));

		User buyer = userRepository.findById(authUser.getId())
			.orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));

		User seller = product.getSeller();

		// 채팅방 및 참가자 객체 생성
		ChatRoomCreationInfo result = chatDomainService.createChatRoom(product, buyer, seller);

		// 참가자 저장
		chatRoomUserRepository.saveAll(result.participants());

		return CreateChatRoomResponse.of(result.chatRoom());
	}

	/**
	 * 채팅방 조회
	 */

	/**
	 * 채팅 기록 조회
	 */

	/**
	 * 채팅방 나가기
	 */

	/**
	 * 메세지 전송.
	 *
	 * @param roomId the room id
	 * @param command the command
	 */
	@Transactional
	public void sendMessage(AuthUser authUser, Long roomId, ChatSendMessageCommand command) {

		// Todo - 메서드 따로 뺴기
		// 채팅방 존재 확인
		ChatRoom chatRoom = chatRoomRepository.findById(roomId)
			.orElseThrow(() -> new ChatException(ChatErrorCode.CHAT_ROOM_NOT_FOUND));

		// 참가자 확인
		boolean isParticipant = chatRoomUserRepository.existsByIdChatRoomIdAndIdUserId(roomId, authUser.getId());
		if (!isParticipant) {
			throw new ChatException(ChatErrorCode.CHAT_ROOM_ACCESS_DENIED);
		}

		// 발신자 정보 조회
		User sender = userRepository.findById(authUser.getId())
			.orElseThrow(() -> new IllegalArgumentException("유저 정보를 찾을 수 없습니다."));

		// 메시지 본문 작성
		ChatMessageInfo message = chatDomainService.createMessage(roomId, sender, command.message(), command.type());

		// 메시지 전송
		messagingTemplate.convertAndSend("/topic/chat/" + roomId, message);

		// 레디스 저장
		chatMessageRedisService.saveMessageToRedis(roomId, message);
	}

	/** 채팅방 Id를 기준으로 레디스에서 채팅기록 꺼내와서 db에 저장
	 *
	 * @param roomId 채팅방 id
	 */
	@Transactional
	public void saveMessagesToDb(Long roomId) {
		// 채팅방Id를 기준으로 레디스에 있는 채팅기록들 불러오기
		List<ChatMessageInfo> messagesFromRedis = chatMessageRedisService.getMessagesFromRedis(roomId);
		if (messagesFromRedis.isEmpty()) return;

		ChatRoom chatRoom = chatRoomRepository.findById(roomId)
			.orElseThrow(() -> new ChatException(ChatErrorCode.CHAT_ROOM_NOT_FOUND));

		List<ChatHistory> histories = new ArrayList<>();

		// 레디스에서 불러온 채팅기록들을 for문을 돌면서 채팅기록 테이블에 저장할 리스트에 추가
		for (ChatMessageInfo messages : messagesFromRedis) {

			User sender = userRepository.findById(messages.senderId())
				.orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));

			ChatHistory history = ChatHistory.builder()
				.chatRoom(chatRoom)
				.userId(sender.getId()) // N + 1 문제 발생
				.message(messages.message())
				.sendtime(messages.sendTime())
				.build();

			histories.add(history);
		}

		chatRoomHistoryRepository.saveAll(histories);

		// db에 저장이 끝난 레디스 데이터 삭제
		chatMessageRedisService.deleteMessagesInRedis(roomId);
	}
}
