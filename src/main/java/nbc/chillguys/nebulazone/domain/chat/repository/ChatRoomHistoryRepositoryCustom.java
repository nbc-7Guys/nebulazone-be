package nbc.chillguys.nebulazone.domain.chat.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import nbc.chillguys.nebulazone.domain.chat.entity.ChatHistory;

public interface ChatRoomHistoryRepositoryCustom {
	Slice<ChatHistory> findAllByChatRoomIdOrderBySendTimeAsc(Long chatRoomId, Long lastId, Pageable pageable);
}
