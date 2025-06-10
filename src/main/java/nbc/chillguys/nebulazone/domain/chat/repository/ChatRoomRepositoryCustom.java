package nbc.chillguys.nebulazone.domain.chat.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import nbc.chillguys.nebulazone.domain.chat.dto.response.ChatRoomInfo;
import nbc.chillguys.nebulazone.domain.chat.entity.ChatRoom;

public interface ChatRoomRepositoryCustom {
	List<ChatRoomInfo> findAllByUserId(Long userId);
}
