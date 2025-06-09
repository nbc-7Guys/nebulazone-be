package nbc.chillguys.nebulazone.domain.chat.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import nbc.chillguys.nebulazone.domain.chat.entity.ChatRoom;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
	boolean existsChatRoomById(Long roomId);

	Optional<ChatRoom> findByProduct_IdAndChatRoomUsers_User_Id(Long productId, Long roomId);
}
