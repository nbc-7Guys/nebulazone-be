package nbc.chillguys.nebulazone.application.chat.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nbc.chillguys.nebulazone.application.chat.dto.request.CreateChatRoomRequest;
import nbc.chillguys.nebulazone.application.chat.dto.response.CreateChatRoomResponse;
import nbc.chillguys.nebulazone.application.chat.dto.response.FindChatHistoryResponse;
import nbc.chillguys.nebulazone.application.chat.dto.response.FindChatRoomResponses;
import nbc.chillguys.nebulazone.application.notification.service.NotificationService;
import nbc.chillguys.nebulazone.domain.chat.dto.response.ChatRoomInfo;
import nbc.chillguys.nebulazone.domain.chat.entity.ChatRoom;
import nbc.chillguys.nebulazone.domain.chat.exception.ChatErrorCode;
import nbc.chillguys.nebulazone.domain.chat.exception.ChatException;
import nbc.chillguys.nebulazone.domain.chat.service.ChatDomainService;
import nbc.chillguys.nebulazone.domain.notification.entity.NotificationType;
import nbc.chillguys.nebulazone.application.notification.dto.NotificationMessage;
import nbc.chillguys.nebulazone.domain.product.entity.Product;
import nbc.chillguys.nebulazone.domain.product.service.ProductDomainService;
import nbc.chillguys.nebulazone.domain.user.entity.User;
import nbc.chillguys.nebulazone.domain.user.service.UserDomainService;

/**
 * 채팅방 관련 비즈니스 로직을 처리하는 애플리케이션 서비스
 *
 * <p>채팅방 생성, 조회, 나가기 등의 핵심 비즈니스 로직을 담당하며,
 * 도메인 서비스와 외부 서비스들을 조합하여 완전한 채팅 기능을 제공.</p>
 *
 * @author 박형우
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

	private final ChatDomainService chatDomainService;
	private final SimpMessagingTemplate messagingTemplate;
	private final ProductDomainService productDomainService;
	private final UserDomainService userDomainService;
	private final NotificationService notificationService;

	/**
	 * 채팅방 생성 또는 기존에 채팅방 조회.<br/>
	 * 채팅방이 없다면 생성하고 있다면 기존 채팅방 정보를 가져온다.<br/>
	 *
	 * @param user the auth user
	 * @param request the request
	 * @return the chat room info
	 */
	@Transactional
	public CreateChatRoomResponse getOrCreate(User user, CreateChatRoomRequest request) {

		// 구매자와 판매자가 동일 유저인지 확인
		validateBuyerIsNotSeller(user.getId(), request.productId());

		// 로그인한 유저가 특정 상품에 대해 참여중인 채팅방이 있는지 확인
		ChatRoom result = chatDomainService.findExistingChatRoom(user.getId(), request.productId())
			.orElseGet(() -> createChatRoom(user, request));

		return CreateChatRoomResponse.from(result);
	}

	/**
	 * 구매자와 판매자가 동일 유저인지 확인
	 *
	 * <p>자기 자신과는 채팅할 수 없도록 유효성을 검증</p>
	 *
	 * @param buyerId 구매자 ID
	 * @param productId 판매 상품 ID
	 * @throws ChatException 구매자와 판매자가 동일한 경우 (CANNOT_CHAT_WITH_SELF)
	 * @author 박형우
	 */
	private void validateBuyerIsNotSeller(Long buyerId, Long productId) {
		Product availableProductById = productDomainService.findAvailableProductById(productId);
		if (availableProductById.getSeller().getId().equals(buyerId)) {
			throw new ChatException(ChatErrorCode.CANNOT_CHAT_WITH_SELF);
		}
	}

	/**
	 * 새로운 채팅방을 생성하고 참여자를 등록
	 *
	 * <p>구매자와 판매자 간의 새로운 채팅방을 생성하고,
	 * 판매자에게 채팅방 생성 알림을 발송</p>
	 *
	 * @param user 채팅방을 생성하는 사용자 (구매자)
	 * @param request 채팅방 생성 요청 데이터
	 * @return 생성된 채팅방 정보
	 * @author 박형우
	 */
	public ChatRoom createChatRoom(User user, CreateChatRoomRequest request) {
		// 기존에 참여중인 방이 없다면 거래상품 구매자, 판매자 생성
		Product product = productDomainService.findAvailableProductById(request.productId());
		User buyer = userDomainService.findActiveUserByEmail(user.getEmail());
		User seller = product.getSeller();

		NotificationMessage notificationMessage = NotificationMessage.of(
			NotificationType.CHAT_ROOM_CREATED,
			"'" + buyer.getNickname() + " 님이 거래 요청을 하였습니다.'",
			"'" + product.getName() + " 제품에 대한 거래를 요청하였습니다.'",
			"/chat/rooms",
			seller.getId(),
			LocalDateTime.now(),
			false
		);

		// 채팅방 및 참가자 save
		ChatRoom chatRoom = chatDomainService.createChatRoom(product, buyer, seller);
		notificationService.sendNotificationToUser(
			seller.getId(),
			notificationMessage
		);

		return chatRoom;
	}

	/**
	 * 유저가 참여중인 모든 채팅방 조회
	 *
	 * @param user the auth user
	 */
	@Transactional(readOnly = true)
	public FindChatRoomResponses findChatRooms(User user) {
		// 로그인한 유저 ID를 기반으로 해당 유저가 참여중인 모든 채팅방 찾기
		List<ChatRoomInfo> chatRooms = chatDomainService.findChatRooms(user);
		return FindChatRoomResponses.of(chatRooms);
	}

	/**
	 * 채팅 기록 조회
	 *
	 * @param user the auth user
	 * @param roomId the room id
	 * @return the find chat room response
	 */
	@Transactional(readOnly = true)
	public List<FindChatHistoryResponse> findChatHistories(User user, Long roomId) {

		chatDomainService.validateUserAccessToChatRoom(user, roomId);

		List<FindChatHistoryResponse> responses = chatDomainService.findChatHistoryResponses(roomId);

		return responses;
	}

	/**
	 * 채팅방 나가기
	 *
	 * <p>사용자가 채팅방에서 나가며, 해당 채팅방의 다른 참여자들에게
	 * 나감 알림 메시지를 WebSocket을 통해 브로드캐스트</p>
	 *
	 * @param user 채팅방을 나가는 사용자
	 * @param roomId 나갈 채팅방 ID
	 * @author 박형우
	 */
	@Transactional
	public void exitChatRoom(User user, Long roomId) {
		String leftUser = chatDomainService.deleteUserFromChatRoom(user.getId(), roomId);
		String message = leftUser + " 님이 채팅방을 나갔습니다.";

		messagingTemplate.convertAndSend("/topic/chat/" + roomId, message);
	}

}
