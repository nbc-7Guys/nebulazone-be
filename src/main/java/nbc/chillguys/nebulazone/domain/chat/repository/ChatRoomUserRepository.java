package nbc.chillguys.nebulazone.domain.chat.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import nbc.chillguys.nebulazone.domain.chat.entity.ChatRoomUser;
import nbc.chillguys.nebulazone.domain.chat.entity.ChatRoomUserId;

public interface ChatRoomUserRepository
	extends JpaRepository<ChatRoomUser, ChatRoomUserId>, ChatRoomUserRepositoryCustom {

	boolean existsByIdChatRoomIdAndIdUserId(Long chatRoomId, Long userId);

}
