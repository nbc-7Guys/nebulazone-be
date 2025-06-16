package nbc.chillguys.nebulazone.application.chat.dto.response;

import java.util.List;

import nbc.chillguys.nebulazone.domain.chat.dto.response.ChatRoomInfo;

public record FindChatRoomResponses(
	List<FindChatRoomResponse> chatRooms
) {
	public static FindChatRoomResponses of(List<ChatRoomInfo> chatRoomInfos) {
		List<FindChatRoomResponse> responses = chatRoomInfos.stream()
			.map(FindChatRoomResponse::from)
			.toList();
		return new FindChatRoomResponses(responses);
	}

	public record FindChatRoomResponse(
		String productName,
		String sellerName,
		Long chatRoomId,
		Long productId,
		Long catalogId,
		Long productPrice,
		boolean isSold
	) {
		public static FindChatRoomResponse from(ChatRoomInfo chatRoomInfo) {
			return new FindChatRoomResponse(
				chatRoomInfo.productName(),
				chatRoomInfo.sellerName(),
				chatRoomInfo.ChatRoomId(),
				chatRoomInfo.productId(),
				chatRoomInfo.catalogId(),
				chatRoomInfo.productPrice(),
				chatRoomInfo.isSold()
			);
		}
	}
}

