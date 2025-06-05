package nbc.chillguys.nebulazone.domain.chat.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import nbc.chillguys.nebulazone.domain.chat.entity.ChatRoom;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

	@Query("""
			SELECT cr
			FROM ChatRoom cr
			JOIN ChatRoomUser buyer ON buyer.chatRoom = cr
			JOIN ChatRoomUser seller ON seller.chatRoom = cr
			WHERE cr.product.id = :productId
			AND seller.user.id = :sellerId
			And buyer.user.id = :buyerId
		""")
	Optional<ChatRoom> findChatRoom(@Param("productId") Long productId, @Param("sellerId") Long sellerId,
		@Param("buyerId") Long buyerId);

	boolean existsChatRoomById(Long roomId);

	List<ChatRoom> findAllByProductId(Long productId);
}
