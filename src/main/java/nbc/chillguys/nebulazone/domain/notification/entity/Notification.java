package nbc.chillguys.nebulazone.domain.notification.entity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "notifications")
@Getter
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "notification_id")
	private Long id;

	@Column(name = "target_user_id", nullable = false)
	private Long targetUserId;

	@Column(name = "target_url", nullable = false)
	private String targetUrl;

	@Column(nullable = false)
	private String title;

	@Column(nullable = false)
	private String content;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, name = "notification_type")
	private NotificationType type;

	@Column(nullable = false, name = "is_read")
	private Boolean isRead = false;

	@CreatedDate
	@Column(nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@Builder
	public Notification(Long targetUserId, String title, String content, NotificationType type, String targetUrl) {
		this.targetUserId = targetUserId;
		this.title = title;
		this.content = content;
		this.targetUrl = targetUrl;
		this.type = type;
	}

	public void markAsRead() {
		this.isRead = true;
	}

}
