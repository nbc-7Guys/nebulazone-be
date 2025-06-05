package nbc.chillguys.nebulazone.domain.chat.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import nbc.chillguys.nebulazone.domain.chat.entity.ChatHistory;

public interface ChatRoomHistoryRepository extends JpaRepository<ChatHistory, Long> {
	List<ChatHistory> findAllByChatRoomIdOrderBySendTimeAsc(Long chatRoomId);

	@Query("""
			SELECT ch
			FROM ChatHistory ch
			WHERE ch.chatRoom.id = :chatRoomId
		""")
	List<ChatHistory> findAllMessageById(Long chatRoomId);
}
