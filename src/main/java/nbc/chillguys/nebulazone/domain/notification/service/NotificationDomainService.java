package nbc.chillguys.nebulazone.domain.notification.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.domain.notification.dto.NotificationInfo;
import nbc.chillguys.nebulazone.application.notification.dto.NotificationMessage;
import nbc.chillguys.nebulazone.domain.notification.entity.Notification;
import nbc.chillguys.nebulazone.domain.notification.exception.NotificationErrorCode;
import nbc.chillguys.nebulazone.domain.notification.exception.NotificationException;
import nbc.chillguys.nebulazone.domain.notification.repository.NotificationRepository;

@Service
@RequiredArgsConstructor
public class NotificationDomainService {

	private final NotificationRepository notificationRepository;

	@Transactional
	public void createNotification(Long userId, NotificationMessage message) {

		Notification notification = Notification.builder()
			.title(message.title())
			.type(message.type())
			.content(message.content())
			.targetUrl(message.targetUrl())
			.targetUserId(userId)
			.build();

		notificationRepository.save(notification);
	}

	@Transactional
	public void readNotification(Long userId, Long notificationId) {
		Notification notification = notificationRepository.findQueryNotificationByUserAndId(userId, notificationId)
			.orElseThrow(() -> new NotificationException(NotificationErrorCode.NOTIFICATION_NOT_FOUND));

		notification.markAsRead();
	}

	public Long readAllNotification(Long userId) {
		return notificationRepository.markAllAsRead(userId);
	}

	public List<NotificationInfo> findUnreadNotifications(Long userId) {
		List<NotificationInfo> notifications = notificationRepository.findQueryAllUnreadNotification(userId);
		return notifications;
	}
}
