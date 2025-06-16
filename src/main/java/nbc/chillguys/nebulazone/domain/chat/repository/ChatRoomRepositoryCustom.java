package nbc.chillguys.nebulazone.domain.chat.repository;

import java.util.List;

import nbc.chillguys.nebulazone.domain.chat.dto.response.ChatRoomInfo;

public interface ChatRoomRepositoryCustom {
	List<ChatRoomInfo> findAllByUserId(Long userId);
}
