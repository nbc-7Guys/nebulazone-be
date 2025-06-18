package nbc.chillguys.nebulazone.domain.notification.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import nbc.chillguys.nebulazone.domain.notification.constant.NotificationType;

public record NotificationMessage(
	NotificationType type,
	String title,
	String content,
	String targetUrl,
	Long targetUserId,
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	LocalDateTime createdAt,
	boolean isRead
) {
	public static NotificationMessage of(
		NotificationType type,
		String title,
		String content,
		String targetUrl,
		Long targetUserId,
		LocalDateTime createdAt,
		boolean isRead
	) {
		return new NotificationMessage(
			type,
			title,
			content,
			targetUrl,
			targetUserId,
			createdAt,
			isRead
		);
	}
}
