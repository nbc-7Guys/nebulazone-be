package nbc.chillguys.nebulazone.domain.chat.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import nbc.chillguys.nebulazone.domain.common.audit.BaseEntity;
import nbc.chillguys.nebulazone.domain.user.entity.User;

@Entity
@Table(name = "chat_histories")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatHistory extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "chat_history_id")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "chat_room_id", nullable = false)
	private ChatRoom chatRoom;

	@Column(name = "user_id", nullable = false)
	private Long userId;

	@Column(nullable = false)
	private String message;

	@Column(nullable = false)
	private LocalDateTime sendTime;

	@Builder
	public ChatHistory(ChatRoom chatRoom, Long userId, String message, LocalDateTime sendtime) {
		this.chatRoom = chatRoom;
		this.userId = userId;
		this.message = message;
		this.sendTime = sendtime;
	}
}
