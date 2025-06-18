package nbc.chillguys.nebulazone.application.notification.controller;

import java.time.LocalDateTime;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.application.notification.service.NotificationService;
import nbc.chillguys.nebulazone.domain.notification.constant.NotificationType;
import nbc.chillguys.nebulazone.domain.notification.dto.NotificationMessage;

@RestController
@RequiredArgsConstructor
public class NotificationController {

	private final NotificationService notificationService;

	@PostMapping("/test/notification")
	public void sendTestNotification(@RequestParam Long userId, @RequestParam String message) {
		notificationService.sendNotificationToUser(userId,
			NotificationMessage.of(
				NotificationType.PRODUCT_PURCHASE,
				"사용자 테스트 알림",
				message, // 사용자가 입력한 메시지
				"/products",
				userId,
				LocalDateTime.now(),
				false
			)
		);
	}

}
