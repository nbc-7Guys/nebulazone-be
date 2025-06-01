package nbc.chillguys.nebulazone.domain.chat.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import nbc.chillguys.nebulazone.domain.chat.entity.ChatHistory;

public interface ChatRoomHistoryRepository extends JpaRepository<ChatHistory, Long> {
	List<ChatHistory> findAllByChatRoomId(Long chatRoomId);
}
