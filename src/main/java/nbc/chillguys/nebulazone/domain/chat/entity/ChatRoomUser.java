package nbc.chillguys.nebulazone.domain.chat.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import nbc.chillguys.nebulazone.domain.user.entity.User;

@Getter
@Entity
@Table(name = "chat_room_users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatRoomUser {

	@EmbeddedId
	private ChatRoomUserId id;

	// EmbeddedId로 식별관계를 만들 때는 @MapsId 어노테이션을 활용해야 함
	// @MapsId는 해당 FK가 복합키(@EmbeddedId)의 어떤 변수인지를 설정할 수 있음
	@MapsId("chatRoomId")
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "chat_room_id", nullable = false)
	private ChatRoom chatRoom;

	@MapsId("userId")
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Builder
	public ChatRoomUser(ChatRoom chatRoom, User user) {
		this.chatRoom = chatRoom;
		this.user = user;
		this.id = ChatRoomUserId.builder()
			.chatRoomId(chatRoom.getId())
			.userId(user.getId())
			.build();
	}

}
