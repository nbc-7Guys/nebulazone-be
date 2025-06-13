package nbc.chillguys.nebulazone.domain.chat.repository;

import java.util.List;

import nbc.chillguys.nebulazone.domain.chat.entity.ChatHistory;

public interface ChatRoomHistoryRepositoryCustom {
	List<ChatHistory> findAllByChatRoomIdOrderBySendTimeAsc(Long chatRoomId);
}
