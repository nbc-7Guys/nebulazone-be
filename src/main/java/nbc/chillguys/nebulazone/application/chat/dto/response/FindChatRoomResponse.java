package nbc.chillguys.nebulazone.application.chat.dto.response;

import java.util.List;

import nbc.chillguys.nebulazone.domain.chat.entity.ChatRoom;

public record FindChatRoomResponse(List<ChatRoomInfo> chatRooms) {
	public static FindChatRoomResponse of(List<ChatRoom> chatRooms) {
		List<ChatRoomInfo> chatRoomInfos = chatRooms.stream().map(ChatRoomInfo::from).toList();

		return new FindChatRoomResponse(chatRoomInfos);
	}

	public record ChatRoomInfo(String productName, String sellerName) {
		public static ChatRoomInfo from(ChatRoom chatRoom) {
			return new ChatRoomInfo(
				chatRoom.getProduct().getName(),
				chatRoom.getProduct().getSeller().getNickname()
			);
		}
	}
}

