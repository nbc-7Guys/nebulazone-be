package nbc.chillguys.nebulazone.application.chat.dto.response;

import lombok.Builder;
import nbc.chillguys.nebulazone.domain.chat.entity.ChatRoom;
import nbc.chillguys.nebulazone.domain.products.entity.Product;
import nbc.chillguys.nebulazone.domain.user.entity.User;

@Builder
public record CreateChatRoomResponse(
	Long chatRoomId,
	Long productId,
	String productName,
	Long productPrice,
	Long sellerId,
	String sellerName
) {
	public static CreateChatRoomResponse of(ChatRoom chatRoom) {
		Product product = chatRoom.getProduct();
		User seller = product.getSeller();

		return CreateChatRoomResponse.builder()
			.chatRoomId(chatRoom.getId())
			.productId(product.getId())
			.productName(product.getName())
			.productPrice(product.getPrice())
			.sellerId(seller.getId())
			.sellerName(seller.getNickname())
			.build();
	}
}
