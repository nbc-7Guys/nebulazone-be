package nbc.chillguys.nebulazone.domain.chat.dto.response;

import nbc.chillguys.nebulazone.domain.chat.entity.ChatRoom;

public record ChatRoomInfo(
	String productName,
	String sellerName,
	Long ChatRoomId
) {
	public static ChatRoomInfo from(ChatRoom chatRoom) {
		return new ChatRoomInfo(
			chatRoom.getProduct().getName(),
			chatRoom.getProduct().getSeller().getNickname(),
			chatRoom.getId()
		);
	}
}
