package nbc.chillguys.nebulazone.domain.chat.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.application.chat.dto.response.FindChatHistoryResponse;
import nbc.chillguys.nebulazone.domain.chat.dto.response.ChatMessageInfo;
import nbc.chillguys.nebulazone.domain.chat.dto.response.ChatRoomInfo;
import nbc.chillguys.nebulazone.domain.chat.entity.ChatHistory;
import nbc.chillguys.nebulazone.domain.chat.entity.ChatRoom;
import nbc.chillguys.nebulazone.domain.chat.entity.ChatRoomUser;
import nbc.chillguys.nebulazone.domain.chat.exception.ChatErrorCode;
import nbc.chillguys.nebulazone.domain.chat.exception.ChatException;
import nbc.chillguys.nebulazone.domain.chat.repository.ChatRoomHistoryRepository;
import nbc.chillguys.nebulazone.domain.chat.repository.ChatRoomRepository;
import nbc.chillguys.nebulazone.domain.chat.repository.ChatRoomUserRepository;
import nbc.chillguys.nebulazone.domain.product.entity.Product;
import nbc.chillguys.nebulazone.domain.product.entity.ProductTxMethod;
import nbc.chillguys.nebulazone.domain.product.exception.ProductErrorCode;
import nbc.chillguys.nebulazone.domain.product.exception.ProductException;
import nbc.chillguys.nebulazone.domain.user.entity.User;

/**
 * The type Chat service.
 */
@Service
@RequiredArgsConstructor
public class ChatDomainService {

	private final ChatRoomRepository chatRoomRepository;
	private final ChatRoomUserRepository chatRoomUserRepository;
	private final ChatRoomHistoryRepository chatRoomHistoryRepository;

	/**
	 * 기존 채팅방을 Optional로 조회
	 *
	 * @param userId 사용자 ID
	 * @param productId 상품 ID
	 * @return Optional(채팅방)
	 */
	public Optional<ChatRoom> findExistingChatRoom(Long userId, Long productId) {
		return chatRoomUserRepository.findByIdUserIdAndChatRoomProductId(userId, productId)
			.map(ChatRoomUser::getChatRoom);
	}

	/**
	 * 참여중인 채팅방들 조회.
	 *
	 * @param user 인증된 사용자
	 * @return 사용자가 참여중인 채팅방들
	 */
	@Transactional(readOnly = true)
	public List<ChatRoomInfo> findChatRooms(User user) {
		List<ChatRoomInfo> chatRooms = chatRoomRepository.findAllByUserId(user.getId());

		return chatRooms;
	}

	/**
	 * 인증된 사용자가 특정 채팅방에 참여중인지 확인<br/>
	 * 참여중이지 않으면 예외를 발생시킴
	 *
	 * @param user 참여 중인지 확인할 인증된 사용자
	 * @param roomId 확인할 채팅방의 ID
	 * @throws ChatException 접근이 거부될 경우 CHAT_ROOM_ACCESS_DENIED 에러 코드와 함께 발생
	 */
	public void validateUserAccessToChatRoom(User user, Long roomId) {
		if (!chatRoomUserRepository.existsByIdChatRoomIdAndIdUserId(roomId, user.getId())) {
			throw new ChatException(ChatErrorCode.CHAT_ROOM_ACCESS_DENIED);
		}
	}

	/**
	 * 지정한 채팅방의 모든 채팅 기록을 조회하여 응답 객체 리스트로 반환
	 *
	 * @param roomId 채팅방 ID
	 * @return 채팅 기록 응답(FindChatHistoryResponse) 리스트
	 */
	@Transactional(readOnly = true)
	public List<FindChatHistoryResponse> findChatHistoryResponses(Long roomId) {
		List<ChatHistory> chatHistory = chatRoomHistoryRepository.findAllByChatRoomIdOrderBySendTimeAsc(roomId);

		List<FindChatHistoryResponse> responses = chatHistory.stream()
			.map(history -> FindChatHistoryResponse.from(history))
			.toList();

		return responses;
	}

	/**
	 * 채팅방 및 참가자 저장
	 * @param product 판매중인 상품
	 * @param buyer 구매자
	 * @param seller 판매자
	 * @return 생성된 채팅방 및 판매자,구매자 정보
	 * @throws ChatException CANNOT_CHAT_WITH_SELF
	 * @throws ProductException INVALID_PRODUCT_TYPE
	 */
	@Transactional
	public ChatRoom createChatRoom(Product product, User buyer, User seller) {

		if (product.getTxMethod().equals(ProductTxMethod.AUCTION)) {
			throw new ProductException(ProductErrorCode.INVALID_PRODUCT_TYPE);
		}

		ChatRoom chatRoom = ChatRoom.builder().product(product).build();
		ChatRoomUser buyerUser = ChatRoomUser.builder().chatRoom(chatRoom).user(buyer).build();
		ChatRoomUser sellerUser = ChatRoomUser.builder().chatRoom(chatRoom).user(seller).build();

		chatRoom.addChatRoomUser(buyerUser);
		chatRoom.addChatRoomUser(sellerUser);

		// 참가자 저장
		chatRoomRepository.save(chatRoom);

		return chatRoom;
	}

	/**
	 * 채팅방 나가기.
	 *
	 * @param userId 채팅방을 나갈 유저ID
	 * @param roomId 유저가 나갈 채팅방ID
	 * @return 채팅방을 나간 유저 닉네임
	 * @throws ChatException CHAT_ROOM_ACCESS_DENIED
	 */
	@Transactional
	public String deleteUserFromChatRoom(Long userId, Long roomId) {

		ChatRoomUser user = chatRoomUserRepository.findByIdUserIdAndIdChatRoomId(userId, roomId)
			.orElseThrow(() -> new ChatException(ChatErrorCode.CHAT_ROOM_ACCESS_DENIED));

		user.getChatRoom().removeChatRoomUser(user);
		chatRoomUserRepository.delete(user);

		return user.getUser().getEmail();
	}

	/**
	 * 채팅 기록 저장
	 *
	 * @param roomId 저장할 채팅방 ID
	 * @param messagesFromRedis 레디스에서 가져온 메시지들
	 * @throws ChatErrorCode CHAT_ROOM_NOT_FOUND
	 */
	public void saveChatHistories(Long roomId, List<ChatMessageInfo> messagesFromRedis) {

		ChatRoom chatRoom = findChatRoom(roomId);

		List<ChatHistory> histories = new ArrayList<>();

		// 레디스에서 불러온 채팅기록들을 for문을 돌면서 채팅기록 테이블에 저장할 리스트에 추가
		for (ChatMessageInfo messages : messagesFromRedis) {

			ChatHistory history = ChatHistory.builder()
				.chatRoom(chatRoom)
				.userId(messages.senderId())
				.message(messages.message())
				.messageType(messages.type())
				.sendtime(messages.sendTime())
				.build();

			histories.add(history);
		}
		chatRoomHistoryRepository.saveAll(histories);
	}

	/**
	 * 채팅방ID로 채팅방 찾기
	 *
	 * @param roomId 채팅방ID
	 * @return ChatRoom 엔티티
	 * @throws ChatException CHAT_ROOM_NOT_FOUND
	 */
	@Transactional(readOnly = true)
	public ChatRoom findChatRoom(Long roomId) {
		ChatRoom chatRoom = chatRoomRepository.findById(roomId)
			.orElseThrow(() -> new ChatException(ChatErrorCode.CHAT_ROOM_NOT_FOUND));
		return chatRoom;
	}

}
