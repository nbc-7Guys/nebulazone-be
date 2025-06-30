package nbc.chillguys.nebulazone.domain.notification.repository;

import static nbc.chillguys.nebulazone.domain.notification.entity.QNotification.*;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.domain.notification.dto.NotificationInfo;
import nbc.chillguys.nebulazone.domain.notification.dto.QNotificationInfo;
import nbc.chillguys.nebulazone.domain.notification.entity.Notification;

@Repository
@RequiredArgsConstructor
public class NotificationRepositoryCustomImpl implements NotificationRepositoryCustom {
	private final JPAQueryFactory jpaQueryFactory;

	@Override
	public List<NotificationInfo> findQueryAllUnreadNotification(Long targetUserId) {
		return jpaQueryFactory
			.select(new QNotificationInfo(
				notification.id,
				notification.type,
				notification.title,
				notification.content,
				notification.targetUrl,
				notification.targetUserId,
				notification.createdAt,
				notification.isRead
			))
			.from(notification)
			.where(
				notification.targetUserId.eq(targetUserId)
			).where(notification.isRead.eq(false))
			.orderBy(notification.createdAt.desc())
			.fetch();
	}

	@Override
	public List<Notification> findAllByTargetUserId(Long targetUserId) {
		return jpaQueryFactory
			.selectFrom(notification)
			.where(
				notification.targetUserId.eq(targetUserId),
				notification.isRead.eq(false)
			)
			.fetch();
	}

	@Override
	public Optional<Notification> findQueryNotificationByUserAndId(Long userId, Long notificationId) {
		return Optional.ofNullable(jpaQueryFactory
				.selectFrom(notification)
				.where(
					notification.targetUserId.eq(userId),
					notification.id.eq(notificationId)
				)
				.fetchOne());
	}

	@Override
	public Long markAllAsRead(Long userId) {
		return jpaQueryFactory
			.update(notification)
			.set(notification.isRead, true)
			.where(
				notification.targetUserId.eq(userId),
				notification.isRead.eq(false)
			)
			.execute();
	}
}
