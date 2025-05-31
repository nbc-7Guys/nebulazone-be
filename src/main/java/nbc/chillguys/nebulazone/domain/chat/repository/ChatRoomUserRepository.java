package nbc.chillguys.nebulazone.domain.chat.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import nbc.chillguys.nebulazone.domain.chat.entity.ChatRoomUser;
import nbc.chillguys.nebulazone.domain.chat.entity.ChatRoomUserId;

public interface ChatRoomUserRepository extends JpaRepository<ChatRoomUser, ChatRoomUserId> {

	boolean existsByIdChatRoomIdAndIdUserId(Long chatRoomId, Long userId);

}
