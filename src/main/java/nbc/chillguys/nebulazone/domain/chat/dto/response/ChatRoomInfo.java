package nbc.chillguys.nebulazone.domain.chat.dto.response;

import nbc.chillguys.nebulazone.domain.chat.entity.ChatRoom;

public record ChatRoomInfo(
	String productName,
	String sellerName,
	Long ChatRoomId,
	Long catalogId,
	Long productId,
	Long productPrice,
	boolean isSold
) {
	public static ChatRoomInfo from(ChatRoom chatRoom) {
		return new ChatRoomInfo(
			chatRoom.getProduct().getName(),
			chatRoom.getProduct().getSeller().getNickname(),
			chatRoom.getId(),
			chatRoom.getProduct().getCatalogId(),
			chatRoom.getProduct().getId(),
			chatRoom.getProduct().getPrice(),
			chatRoom.getProduct().isSold()
		);
	}
}
