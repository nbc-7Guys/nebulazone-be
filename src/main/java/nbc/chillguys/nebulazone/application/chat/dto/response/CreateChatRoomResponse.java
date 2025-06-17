package nbc.chillguys.nebulazone.application.chat.dto.response;

import nbc.chillguys.nebulazone.domain.catalog.entity.Catalog;
import nbc.chillguys.nebulazone.domain.chat.entity.ChatRoom;
import nbc.chillguys.nebulazone.domain.product.entity.Product;
import nbc.chillguys.nebulazone.domain.user.entity.User;

public record CreateChatRoomResponse(
	Long chatRoomId,
	Long productId,
	Long catalogId,
	String productName,
	Long productPrice,
	Long sellerId,
	String sellerName
) {
	public static CreateChatRoomResponse from(ChatRoom chatRoom) {
		Product product = chatRoom.getProduct();
		User seller = product.getSeller();
		Catalog catalog = product.getCatalog();

		return new CreateChatRoomResponse(
			chatRoom.getId(),
			product.getId(),
			catalog.getId(),
			product.getName(),
			product.getPrice(),
			seller.getId(),
			seller.getNickname()
		);
	}
}
