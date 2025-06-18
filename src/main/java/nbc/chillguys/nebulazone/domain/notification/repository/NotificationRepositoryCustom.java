package nbc.chillguys.nebulazone.domain.notification.repository;

import java.util.List;
import java.util.Optional;

import nbc.chillguys.nebulazone.domain.notification.dto.NotificationInfo;
import nbc.chillguys.nebulazone.domain.notification.entity.Notification;

public interface NotificationRepositoryCustom {
	List<NotificationInfo> findQueryAllUnreadNotification(Long targetUserId);

	List<Notification> findAllByTargetUserId(Long targetUserId);

	Optional<Notification> findQueryNotificationByUserAndId(Long userId, Long notificationId);

}
