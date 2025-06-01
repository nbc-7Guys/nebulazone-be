package nbc.chillguys.nebulazone.application.chat.dto.response;

import java.util.List;

import lombok.Builder;
import nbc.chillguys.nebulazone.domain.chat.entity.ChatRoom;

@Builder
public record FindChatRoomResponse(
	List<ChatRoomInfo> chatRooms
) {
	@Builder
	public record ChatRoomInfo(
		String productName,
		String sellerName
	) {
	}

	public static FindChatRoomResponse of(List<ChatRoom> chatRooms) {
		List<ChatRoomInfo> chatRoomInfos = chatRooms.stream()
			.map(chatRoom -> ChatRoomInfo.builder()
				.productName(chatRoom.getProduct().getName())
				.sellerName(chatRoom.getProduct().getSeller().getNickname())
				.build())
			.toList();
		return FindChatRoomResponse.builder()
			.chatRooms(chatRoomInfos)
			.build();
	}
}

