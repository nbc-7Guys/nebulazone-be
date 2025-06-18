package nbc.chillguys.nebulazone.application.notification.dto;

import java.time.LocalDateTime;
import java.util.List;

import nbc.chillguys.nebulazone.domain.notification.dto.NotificationInfo;
import nbc.chillguys.nebulazone.domain.notification.entity.NotificationType;

public record UnreadNotificationResponses(
	List<UnreadNotificationResponse> notifications
) {

	public static UnreadNotificationResponses of(List<NotificationInfo> notificationInfos) {
		return new UnreadNotificationResponses(
			notificationInfos.stream()
				.map(UnreadNotificationResponse::from)
				.toList()
		);
	}

	public record UnreadNotificationResponse(
		Long id,
		NotificationType type,
		String title,
		String content,
		String targetUrl,
		Long targetUserId,
		LocalDateTime createdAt,
		boolean isRead
	) {
		public static UnreadNotificationResponse from(NotificationInfo notificationInfo) {
			return new UnreadNotificationResponse(
				notificationInfo.id(),
				notificationInfo.type(),
				notificationInfo.title(),
				notificationInfo.content(),
				notificationInfo.targetUrl(),
				notificationInfo.targetUserId(),
				notificationInfo.createdAt(),
				notificationInfo.isRead()
			);
		}
	}
}
