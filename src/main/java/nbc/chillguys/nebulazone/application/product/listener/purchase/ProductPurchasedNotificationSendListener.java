package nbc.chillguys.nebulazone.application.product.listener.purchase;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.application.notification.service.NotificationService;
import nbc.chillguys.nebulazone.domain.product.entity.Product;
import nbc.chillguys.nebulazone.domain.product.event.ProductPurchasedEvent;
import nbc.chillguys.nebulazone.domain.user.entity.User;

@RequiredArgsConstructor
@Component
public class ProductPurchasedNotificationSendListener {

	private final NotificationService notificationService;

	@Async
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handle(ProductPurchasedEvent event) {
		Product product = event.product();
		User buyer = event.buyer();

		notificationService.sendProductPurchaseNotification(
			product.getId(), product.getSellerId(), buyer.getId(), product.getName(), buyer.getNickname()
		);
	}
}
