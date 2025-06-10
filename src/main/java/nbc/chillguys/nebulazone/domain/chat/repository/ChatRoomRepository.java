package nbc.chillguys.nebulazone.domain.chat.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import nbc.chillguys.nebulazone.domain.chat.entity.ChatRoom;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long>, ChatRoomRepositoryCustom {
	boolean existsChatRoomById(Long roomId);
}
