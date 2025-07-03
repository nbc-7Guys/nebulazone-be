package nbc.chillguys.nebulazone.application.notification.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nbc.chillguys.nebulazone.application.notification.dto.NotificationMessage;
import nbc.chillguys.nebulazone.application.notification.dto.UnreadNotificationResponses;
import nbc.chillguys.nebulazone.domain.notification.dto.NotificationInfo;
import nbc.chillguys.nebulazone.domain.notification.entity.NotificationType;
import nbc.chillguys.nebulazone.domain.notification.service.NotificationDomainService;
import nbc.chillguys.nebulazone.domain.user.entity.User;
import nbc.chillguys.nebulazone.infra.redis.service.WebSocketSessionRedisService;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

	private final SimpMessagingTemplate messagingTemplate;
	private final WebSocketSessionRedisService sessionRedisService;
	private final NotificationDomainService notificationDomainService;
	private final EntityManager em;

	/**
	 * 단일 사용자에게 알림 메시지를 전송
	 * 사용자가 온라인 상태인 경우 WebSocket을 통해 즉시 전송하고,
	 * 오프라인 상태이거나 전송 여부와 관계없이 알림을 저장.
	 * @param userId 알림을 받을 사용자의 ID
	 * @param message 전송할 알림 메시지 객체
	 */
	public void sendNotificationToUser(Long userId, NotificationMessage message) {
		try {
			if (sessionRedisService.isOnlineUser(userId)) {
				String destination = "/topic/notification/" + userId;
				messagingTemplate.convertAndSend(destination, message);
				log.info("알림 전송 성공 - userId : {}, type = {}, title = {}", userId, message.type(), message.title());
			} else {
				log.info("사용자 오프라인 - userId : {}", userId);
			}

			notificationDomainService.createNotification(userId, message);

		} catch (Exception e) {
			log.error("알림 전송 실패 - userId : {}, message : {}", userId, message, e);
		}
	}

	/**
	 * 여러 사용자에게 알림 메시지를 전송
	 * 주로 경매와 같이 여러 사용자에게 동시에 알림을 보내야 하는 경우에 사용
	 * 각 사용자에게 개별적으로 알림을 전송하는 {@code sendNotificationToUser} 메서드를 호출
	 * @param userIds 알림을 받을 사용자 ID 목록
	 * @param message 전송할 알림 메시지 객체
	 */
	public void sendNotificationToUsers(List<Long> userIds, NotificationMessage message) {
		userIds.forEach(userId -> {
			NotificationMessage notificationMessage = NotificationMessage.of(
				message.type(),
				message.title(),
				message.content(),
				message.targetUrl(),
				userId,
				message.createdAt(),
				false
			);
			sendNotificationToUser(userId, notificationMessage);
		});
	}

	/**
	 * 상품 구매 완료 알림을 판매자와 구매자에게 전송
	 *
	 * @param productId 구매된 상품의 ID
	 * @param sellerId 상품 판매자의 ID
	 * @param buyerId 상품 구매자의 ID
	 * @param productName 구매된 상품의 이름
	 * @param buyerName 상품 구매자의 이름
	 */
	public void sendProductPurchaseNotification(
		Long productId,
		Long sellerId,
		Long buyerId,
		String productName,
		String buyerName
	) {
		try {
			// 판매자에게 알림
			NotificationMessage sellerNotification = NotificationMessage.of(
				NotificationType.PRODUCT_PURCHASE,
				"상품 판매 완료",
				buyerName + "님이 '" + productName + "' 상품을 구매했습니다.",
				"/transactions",
				sellerId,
				LocalDateTime.now(),
				false
			);

			sendNotificationToUser(sellerId, sellerNotification);

			// 구매자에게 알림
			NotificationMessage buyerNotification = NotificationMessage.of(
				NotificationType.PRODUCT_PURCHASE,
				"구매 완료",
				"'" + productName + "' 상품 구매가 완료되었습니다.",
				"/transactions",
				buyerId,
				LocalDateTime.now(),
				false
			);

			sendNotificationToUser(buyerId, buyerNotification);

		} catch (Exception e) {
			log.error("상품 구매 알림 실패 - productId={}, error={}", productId, e.getMessage());
			throw new RuntimeException(e);
		}
	}

	/**
	 * 사용자의 읽지 않은 알림을 조회
	 * @param user 알림을 조회할 사용자 객체
	 * @return 읽지 않은 알림 목록을 담은 응답 객체
	 */
	public UnreadNotificationResponses findUnreadNotifications(User user) {
		List<NotificationInfo> unreadNotifications = notificationDomainService.findUnreadNotifications(
			user.getId());
		return UnreadNotificationResponses.of(unreadNotifications);
	}

	/**
	 * 특정 알림을 읽음 상태로 표시
	 *
	 * @param user 알림을 읽음 처리할 사용자 객체
	 * @param notificationId 읽음 처리할 알림의 ID
	 */
	public void markNotificationAsRead(User user, Long notificationId) {
		notificationDomainService.readNotification(user.getId(), notificationId);
	}

	/**
	 * 사용자의 모든 알림을 읽음 상태로 표시
	 * @param user 모든 알림을 읽음 처리할 사용자 객체
	 * @return 읽음 처리된 알림의 개수
	 */
	@Transactional
	public Long markAllNotificationAsRead(User user) {
		Long readAllNotification = notificationDomainService.readAllNotification(user.getId());

		if (readAllNotification > 0) {
			em.flush();
			em.clear();
		}

		return readAllNotification;
	}

}
