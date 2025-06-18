package nbc.chillguys.nebulazone.domain.notification.dto;

import java.time.LocalDateTime;

import com.querydsl.core.annotations.QueryProjection;

import nbc.chillguys.nebulazone.domain.notification.entity.NotificationType;

public record NotificationInfo(
	Long id,
	NotificationType type,
	String title,
	String content,
	String targetUrl,
	Long targetUserId,
	LocalDateTime createdAt,
	boolean isRead
) {
	@QueryProjection
	public NotificationInfo {
	}
}
