package nbc.chillguys.nebulazone.application.notification.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nbc.chillguys.nebulazone.domain.notification.constant.NotificationType;
import nbc.chillguys.nebulazone.domain.notification.dto.NotificationMessage;
import nbc.chillguys.nebulazone.domain.notification.service.NotificationDomainService;
import nbc.chillguys.nebulazone.infra.redis.service.WebSocketSessionRedisService;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

	private final SimpMessagingTemplate messagingTemplate;
	private final WebSocketSessionRedisService sessionRedisService;
	private final NotificationDomainService notificationDomainService;

	// 단일 유저 에게 메세지 전송
	public void sendNotificationToUser(Long userId, NotificationMessage message) {
		try {
			if (sessionRedisService.isOnlineUser(userId)) {
				String destination = "/topic/notification/" + userId;
				messagingTemplate.convertAndSend(destination, message);
				log.info("알림 전송 성공 - userId : {}, type = {}, title = {}", userId, message.type(), message.title());
			} else {
				log.info("사용자 오프라인 - userId : {}", userId);
			}
		} catch (Exception e) {
			log.error("알림 전송 실패 - userId : {}, message : {}", userId, message, e);
		}
	}

	// 여러 유저 에게 메세지 전송 - 경매 에서 사용
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
				"상품 판매",
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
		}
	}

}
