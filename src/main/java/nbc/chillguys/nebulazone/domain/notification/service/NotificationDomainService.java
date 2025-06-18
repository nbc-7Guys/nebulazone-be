package nbc.chillguys.nebulazone.domain.notification.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.domain.notification.dto.NotificationInfo;
import nbc.chillguys.nebulazone.application.notification.dto.NotificationMessage;
import nbc.chillguys.nebulazone.domain.notification.entity.Notification;
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
		Notification notification = notificationRepository.findById(notificationId)
			.orElseThrow(() -> new RuntimeException("해당 알림은 존재하지 않습니다."));

		notification.markAsRead();
	}

	@Transactional
	public void readAllNotification(Long userId) {
		List<Notification> allNotifications = notificationRepository.findAllByTargetUserId(userId);
		allNotifications.forEach(Notification::markAsRead);
	}

	public List<NotificationInfo> findUnreadNotifications(Long userId) {
		List<NotificationInfo> notifications = notificationRepository.findQueryAllUnreadNotification(userId);
		return notifications;
	}
}
