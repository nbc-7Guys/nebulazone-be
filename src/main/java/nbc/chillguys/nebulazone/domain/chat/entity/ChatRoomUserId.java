package nbc.chillguys.nebulazone.domain.chat.entity;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatRoomUserId implements Serializable {

	@Column(name = "chat_room_id", nullable = false)
	private Long chatRoomId;

	@Column(name = "user_id", nullable = false)
	private Long userId;

}
