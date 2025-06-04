package nbc.chillguys.nebulazone.application.chat.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nbc.chillguys.nebulazone.application.chat.dto.request.CreateChatRoomRequest;
import nbc.chillguys.nebulazone.application.chat.dto.response.FindChatHistoryResponse;
import nbc.chillguys.nebulazone.application.chat.dto.response.FindChatRoomResponse;
import nbc.chillguys.nebulazone.domain.auth.vo.AuthUser;
import nbc.chillguys.nebulazone.domain.chat.dto.response.ChatRoomCreationInfo;
import nbc.chillguys.nebulazone.domain.chat.dto.request.ChatSendMessageCommand;
import nbc.chillguys.nebulazone.domain.chat.dto.response.ChatMessageInfo;
import nbc.chillguys.nebulazone.application.chat.dto.response.CreateChatRoomResponse;
import nbc.chillguys.nebulazone.domain.chat.entity.ChatHistory;
import nbc.chillguys.nebulazone.domain.chat.entity.ChatRoom;
import nbc.chillguys.nebulazone.domain.chat.entity.ChatRoomUser;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

	private final ChatRoomRepository chatRoomRepository;
	private final ProductRepository productRepository;
	private final UserRepository userRepository;
	private final ChatRoomUserRepository chatRoomUserRepository;
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

		/**
		 * 구매자가 채팅을 한 후 채팅방을 나가지 않고 다시 거래페이지에서 채팅하기 버튼을 눌렀을 경우
		 * 1. 채팅방 존재 확인
		 * 2. 존재하면 현재 유저가 참여중인지 확인
		 * 3. 참여 중이면 -> 기존 방 반환
		 * 4. 존재하지 않거나 참여 중이 아니면 -> 새 채팅방 생성 및 참여자 등록
		 */

		// 로그인한 유저가 특정 상품에 대해 참여중인 채팅방이 있는지 확인
		Optional<ChatRoomUser> existingChatRoomUser = chatRoomUserRepository.findByIdUserIdAndChatRoomProductId(
			authUser.getId(), request.productId());
		if (existingChatRoomUser.isPresent()) {
			return CreateChatRoomResponse.of(existingChatRoomUser.get().getChatRoom());
		}

		// 기존에 참여중인 방이 없다면 새 채팅방 생성
		Product product = productRepository.findById(request.productId())
			.orElseThrow(() -> new RuntimeException("상품을 찾을 수 없습니다.")); // Todo - ProductException으로 변경

		User buyer = userRepository.findById(authUser.getId())
			.orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));

		User seller = product.getSeller();

		// 채팅방 및 참가자 객체 생성
		ChatRoomCreationInfo result = chatDomainService.createChatRoom(product, buyer, seller);

		log.info("ChatRoomCreationInfo: {}", result);

		// 참가자 저장
		chatRoomRepository.save(result.chatRoom());
		chatRoomUserRepository.saveAll(result.participants());

		return CreateChatRoomResponse.of(result.chatRoom());
	}

	/**
	 * 유저가 참여중인 모든 채팅방 조회
	 *
	 * @param authUser the auth user
	 */
	public FindChatRoomResponse findChatRooms(AuthUser authUser) {
		// 로그인한 유저 ID를 기반으로 해당 유저가 참여중인 모든 채팅방 찾기
		List<ChatRoomUser> chatRoomUsers = chatRoomUserRepository.findAllByUserId(authUser.getId());

		log.info("ChatRoomUsers: {}", chatRoomUsers);

		List<ChatRoom> chatRooms = chatRoomUsers.stream()
			.map(ChatRoomUser::getChatRoom)
			.toList();

		return FindChatRoomResponse.of(chatRooms);
	}

	/**
	 * 채팅 기록 조회
	 *
	 * @param authUser the auth user
	 * @param roomId the room id
	 * @return the find chat room response
	 */
	public List<FindChatHistoryResponse> findChatHistories(AuthUser authUser, Long roomId) {
		// 채팅기록 조회
		if (!chatRoomUserRepository.existsByIdChatRoomIdAndIdUserId(roomId, authUser.getId())) {
			throw new ChatException(ChatErrorCode.CHAT_ROOM_ACCESS_DENIED);
		}

		List<ChatHistory> chatHistory = chatRoomHistoryRepository.findAllByChatRoomId(roomId);

		List<Long> senderIdList = chatHistory.stream().map(ChatHistory::getUserId).distinct().toList();

		Map<Long, String> userNicknames = userRepository.findAllById(senderIdList)
			.stream()
			.collect(Collectors.toMap(User::getId, User::getNickname));

		List<FindChatHistoryResponse> responses = chatHistory.stream()
			.map(history -> FindChatHistoryResponse.builder()
				.message(history.getMessage())
				.sender(userNicknames.get(history.getUserId()))
				.sendTime(history.getSendtime())
				.build())
			.toList();

		return responses;
	}

	/**
	 * 채팅방 나가기
	 * @param authUser the auth user
	 * @param roomId the room id
	 */
	@Transactional
	public void leaveChatRoom(AuthUser authUser, Long roomId) {
		ChatRoomUser user = chatRoomUserRepository.findByIdUserIdAndIdChatRoomId(authUser.getId(), roomId)
			.orElseThrow(() -> new ChatException(ChatErrorCode.CHAT_ROOM_ACCESS_DENIED));

		chatRoomUserRepository.delete(user);

		messagingTemplate.convertAndSend("/topic/chat/" + roomId, "상대방이 채팅방을 나갔습니다.");
	}

}
