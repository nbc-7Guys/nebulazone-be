package nbc.chillguys.nebulazone.domain.chat.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import nbc.chillguys.nebulazone.domain.chat.entity.ChatRoomUser;
import nbc.chillguys.nebulazone.domain.chat.entity.ChatRoomUserId;

public interface ChatRoomUserRepositoryCustom {
	List<ChatRoomUser> findAllByUserId(@Param("userId") Long userId);

	Optional<ChatRoomUser> findByIdUserIdAndIdChatRoomId(Long userId, Long chatRoomId);

	Optional<ChatRoomUser> findByIdUserIdAndChatRoomProductId(Long userId, Long productId);

}
