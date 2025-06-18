package nbc.chillguys.nebulazone.application.notification.controller;

import java.time.LocalDateTime;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.application.notification.dto.UnreadNotificationResponses;
import nbc.chillguys.nebulazone.application.notification.service.NotificationService;
import nbc.chillguys.nebulazone.domain.auth.vo.AuthUser;
import nbc.chillguys.nebulazone.domain.notification.entity.NotificationType;
import nbc.chillguys.nebulazone.application.notification.dto.NotificationMessage;

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

	@GetMapping("/notifications")
	public ResponseEntity<UnreadNotificationResponses> findUnreadNotifications(
		@AuthenticationPrincipal AuthUser authUser
	) {
		UnreadNotificationResponses unreadNotifications = notificationService.findUnreadNotifications(authUser);
		return ResponseEntity.ok(unreadNotifications);
	}

	@PatchMapping("/notification/{notificationId}/read")
	public ResponseEntity<Void> markNotificationAsRead(
		@AuthenticationPrincipal AuthUser authUser,
		@PathVariable("notificationId") Long notificationId
	) {
		notificationService.markNotificationAsRead(authUser, notificationId);
		return ResponseEntity.ok().build();
	}

	@PatchMapping("/notification/read-all")
	public ResponseEntity<Void> markAllNotificationAsRead(
		@AuthenticationPrincipal AuthUser authUser
	) {
		notificationService.markAllNotificationAsRead(authUser);
		return ResponseEntity.ok().build();
	}

}
