package nbc.chillguys.nebulazone.domain.chat.dto.response;

import java.util.List;

import lombok.Builder;
import nbc.chillguys.nebulazone.domain.chat.entity.ChatRoom;
import nbc.chillguys.nebulazone.domain.chat.entity.ChatRoomUser;

@Builder
public record ChatRoomCreationInfo(
	ChatRoom chatRoom,
	List<ChatRoomUser> participants
) {
}
