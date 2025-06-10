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

    public static record FindChatRoomResponse(
        String productName,
        String sellerName
    ) {
        public static FindChatRoomResponse from(ChatRoomInfo chatRoomInfo) {
            return new FindChatRoomResponse(
                chatRoomInfo.productName(),
                chatRoomInfo.sellerName()
            );
        }
    }
}

