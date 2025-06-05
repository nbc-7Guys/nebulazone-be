package nbc.chillguys.nebulazone.domain.chat.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.domain.auth.vo.AuthUser;
import nbc.chillguys.nebulazone.domain.chat.dto.response.ChatRoomCreationInfo;
import nbc.chillguys.nebulazone.domain.chat.dto.response.ChatMessageInfo;
import nbc.chillguys.nebulazone.domain.chat.entity.ChatRoom;
import nbc.chillguys.nebulazone.domain.chat.entity.ChatRoomUser;
import nbc.chillguys.nebulazone.domain.chat.exception.ChatErrorCode;
import nbc.chillguys.nebulazone.domain.chat.exception.ChatException;
import nbc.chillguys.nebulazone.domain.chat.repository.ChatRoomHistoryRepository;
import nbc.chillguys.nebulazone.domain.chat.repository.ChatRoomRepository;
import nbc.chillguys.nebulazone.domain.chat.repository.ChatRoomUserRepository;
import nbc.chillguys.nebulazone.domain.products.entity.Product;
import nbc.chillguys.nebulazone.domain.products.entity.ProductTxMethod;
import nbc.chillguys.nebulazone.domain.products.exception.ProductErrorCode;
import nbc.chillguys.nebulazone.domain.products.exception.ProductException;
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

	public Optional<ChatRoomUser> findExistingChatRoom(Long userId, Long productId) {
		return chatRoomUserRepository.findByIdUserIdAndChatRoomProductId(
			userId, productId);
	}

	/**
	 * 채팅방 및 참가자 저장
	 */
	@Transactional
	public ChatRoomCreationInfo createChatRoom(Product product, User buyer, User seller) {

		if (product.isSold() || product.isDeleted()) {
			throw new ChatException(ChatErrorCode.PRODUCT_SOLD_OUT);
		}
		if (buyer.getId().equals(seller.getId())) {
			throw new ChatException(ChatErrorCode.CANNOT_CHAT_WITH_SELF);
		}
		if (product.getTxMethod().equals(ProductTxMethod.AUCTION)) {
			throw new ProductException(ProductErrorCode.INVALID_PRODUCT_TYPE);
		}

		ChatRoom chatRoom = ChatRoom.builder()
			.product(product)
			.build();

		ChatRoomUser buyerUser = ChatRoomUser.builder()
			.chatRoom(chatRoom)
			.user(buyer)
			.build();

		ChatRoomUser sellerUser = ChatRoomUser.builder()
			.chatRoom(chatRoom)
			.user(seller)
			.build();

		List<ChatRoomUser> chatRoomUsers = List.of(buyerUser, sellerUser);

		// 참가자 저장
		chatRoomRepository.save(chatRoom);
		chatRoomUserRepository.saveAll(chatRoomUsers);

		return ChatRoomCreationInfo.builder()
			.chatRoom(chatRoom)
			.build();
	}

	/**
	 * 채팅방 메시지 생성
	 */
	public ChatMessageInfo createMessage(Long roomId, AuthUser sender, String content, String type) {
		return ChatMessageInfo.builder()
            .roomId(roomId)
			.senderId(sender.getId())
            .senderEmail(sender.getEmail())
            .message(content)
            .sendTime(LocalDateTime.now())
            .build();
	}

	public String removeUserFromChatRoom(Long userId, Long roomId) {

		ChatRoomUser user = chatRoomUserRepository.findByIdUserIdAndIdChatRoomId(userId, roomId)
			.orElseThrow(() -> new ChatException(ChatErrorCode.CHAT_ROOM_ACCESS_DENIED));

		chatRoomUserRepository.delete(user);

		return user.getUser().getNickname();
	}

}
