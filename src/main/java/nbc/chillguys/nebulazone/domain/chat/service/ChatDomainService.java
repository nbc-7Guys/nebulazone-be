package nbc.chillguys.nebulazone.domain.chat.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.stereotype.Service;

import nbc.chillguys.nebulazone.domain.chat.dto.response.ChatRoomCreationInfo;
import nbc.chillguys.nebulazone.domain.chat.dto.response.ChatMessageInfo;
import nbc.chillguys.nebulazone.domain.chat.entity.ChatRoom;
import nbc.chillguys.nebulazone.domain.chat.entity.ChatRoomUser;
import nbc.chillguys.nebulazone.domain.products.entity.Product;
import nbc.chillguys.nebulazone.domain.user.entity.User;

/**
 * The type Chat service.
 */
@Service
public class ChatDomainService {

	/**
	 * 채팅방 및 참가자 객체 생성
	 */
	public ChatRoomCreationInfo createChatRoom(Product product, User buyer, User seller) {

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

		ChatRoomCreationInfo result = ChatRoomCreationInfo.builder()
			.chatRoom(chatRoom)
			.participants(chatRoomUsers)
			.build();

		return result;
	}

	/**
	 * 채팅방 메시지 생성
	 */
	public ChatMessageInfo createMessage(Long roomId, User sender, String content, String type) {
		return ChatMessageInfo.builder()
            .roomId(roomId)
            .senderId(sender.getId())
            .message(content)
            .sendTime(LocalDateTime.now())
            .build();
	}

}
