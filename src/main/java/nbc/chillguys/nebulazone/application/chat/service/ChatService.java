package nbc.chillguys.nebulazone.application.chat.service;

import java.util.List;
import java.util.Optional;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nbc.chillguys.nebulazone.application.chat.dto.request.CreateChatRoomRequest;
import nbc.chillguys.nebulazone.application.chat.dto.response.CreateChatRoomResponse;
import nbc.chillguys.nebulazone.application.chat.dto.response.FindChatHistoryResponse;
import nbc.chillguys.nebulazone.application.chat.dto.response.FindChatRoomResponse;
import nbc.chillguys.nebulazone.domain.auth.vo.AuthUser;
import nbc.chillguys.nebulazone.domain.chat.entity.ChatRoom;
import nbc.chillguys.nebulazone.domain.chat.entity.ChatRoomUser;
import nbc.chillguys.nebulazone.domain.chat.service.ChatDomainService;
import nbc.chillguys.nebulazone.domain.products.entity.Product;
import nbc.chillguys.nebulazone.domain.products.service.ProductDomainService;
import nbc.chillguys.nebulazone.domain.user.entity.User;
import nbc.chillguys.nebulazone.domain.user.service.UserDomainService;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

	private final ChatDomainService chatDomainService;
	private final SimpMessagingTemplate messagingTemplate;
	private final ProductDomainService productDomainService;
	private final UserDomainService userDomainService;

	/**
	 * 채팅방 생성 또는 기존에 채팅방 조회.<br/>
	 * 채팅방이 없다면 생성하고 있다면 기존 채팅방 정보를 가져온다.<br/>
	 *
	 * @param authUser the auth user
	 * @param request the request
	 * @return the chat room info
	 */
	public CreateChatRoomResponse getOrCreate(AuthUser authUser, CreateChatRoomRequest request) {

		// 로그인한 유저가 특정 상품에 대해 참여중인 채팅방이 있는지 확인
		// Optional<ChatRoomUser> existingChatRoomUser = chatDomainService.findExistingChatRoom(authUser.getId(),
		// 	request.productId());
		// if (existingChatRoomUser.isPresent()) {
		// 	return CreateChatRoomResponse.from(existingChatRoomUser.get().getChatRoom());
		// }

		ChatRoom result = chatDomainService.findChatRoom(request.productId(), authUser.getId())
			.orElse(createChatRoom(authUser, request));

		return CreateChatRoomResponse.from(result);
	}

	@Transactional
	public ChatRoom createChatRoom(AuthUser authUser, CreateChatRoomRequest request) {
		// 기존에 참여중인 방이 없다면 거래상품 구매자, 판매자 생성
		Product product = productDomainService.findAvailableProductById(request.productId());
		User buyer = userDomainService.findActiveUserByEmail(authUser.getEmail());
		User seller = product.getSeller();

		// 채팅방 및 참가자 save
		return chatDomainService.createChatRoom(product, buyer, seller);
	}

	/**
	 * 유저가 참여중인 모든 채팅방 조회
	 *
	 * @param authUser the auth user
	 */
	public FindChatRoomResponse findChatRooms(AuthUser authUser) {
		// 로그인한 유저 ID를 기반으로 해당 유저가 참여중인 모든 채팅방 찾기
		List<ChatRoom> chatRooms = chatDomainService.findChatRooms(authUser);
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

		chatDomainService.validateUserAccessToChatRoom(authUser, roomId);

		List<FindChatHistoryResponse> responses = chatDomainService.findChatHistoryResponses(roomId);

		return responses;
	}

	/**
	 * 채팅방 나가기
	 * @param authUser the auth user
	 * @param roomId the room id
	 */
	@Transactional
	public void exitChatRoom(AuthUser authUser, Long roomId) {
		String leftUser = chatDomainService.deleteUserFromChatRoom(authUser.getId(), roomId);
		String message = leftUser + " 님이 채팅방을 나갔습니다.";

		messagingTemplate.convertAndSend("/topic/chat/" + roomId, message);
	}

}
