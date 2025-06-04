package nbc.chillguys.nebulazone.domain.chat.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import nbc.chillguys.nebulazone.domain.chat.entity.ChatRoomUser;
import nbc.chillguys.nebulazone.domain.chat.entity.ChatRoomUserId;

public interface ChatRoomUserRepository extends JpaRepository<ChatRoomUser, ChatRoomUserId> {

	boolean existsByIdChatRoomIdAndIdUserId(Long chatRoomId, Long userId);

	@Query("""
		    SELECT cru
			FROM ChatRoomUser cru
		    JOIN FETCH cru.chatRoom cr
		    JOIN FETCH cr.product
		    JOIN FETCH cru.user
		    WHERE cru.user.id = :userId
		""")
	List<ChatRoomUser> findAllByUserId(@Param("userId") Long userId);

	Optional<ChatRoomUser> findByIdUserIdAndIdChatRoomId(Long userId, Long chatRoomId);

	@Query("""
		  SELECT cru
		  FROM ChatRoomUser cru
		  JOIN cru.chatRoom r
		  WHERE cru.id.userId = :userId
		  AND r.product.id = :productId
		""")
	Optional<ChatRoomUser> findByIdUserIdAndChatRoomProductId(Long userId, Long productId);
}
