package nbc.chillguys.nebulazone.application.notification.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.application.notification.dto.NotificationMessage;
import nbc.chillguys.nebulazone.application.notification.dto.UnreadNotificationResponses;
import nbc.chillguys.nebulazone.application.notification.service.NotificationService;
import nbc.chillguys.nebulazone.domain.notification.entity.NotificationType;
import nbc.chillguys.nebulazone.domain.user.entity.User;

@RestController
@RequiredArgsConstructor
public class NotificationController {

	private final NotificationService notificationService;

	@GetMapping("/notification")
	public ResponseEntity<UnreadNotificationResponses> findUnreadNotifications(
		@AuthenticationPrincipal User user
	) {
		UnreadNotificationResponses unreadNotifications = notificationService.findUnreadNotifications(user);
		return ResponseEntity.ok(unreadNotifications);
	}

	@PatchMapping("/notification/{notificationId}/read")
	public ResponseEntity<Void> markNotificationAsRead(
		@AuthenticationPrincipal User user,
		@PathVariable("notificationId") @NotBlank(message = "notificationId를 입력해 주세요") Long notificationId
	) {
		notificationService.markNotificationAsRead(user, notificationId);
		return ResponseEntity.ok().build();
	}

	@PatchMapping("/notification/read-all")
	public ResponseEntity<Long> markAllNotificationAsRead(
		@AuthenticationPrincipal User user
	) {
		Long totalMarkedAsRead = notificationService.markAllNotificationAsRead(user);
		return ResponseEntity.ok().body(totalMarkedAsRead);
	}

}
