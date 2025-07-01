package nbc.chillguys.nebulazone.application.product.listener;

import java.time.LocalDateTime;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.application.notification.service.NotificationService;
import nbc.chillguys.nebulazone.domain.product.entity.Product;
import nbc.chillguys.nebulazone.domain.product.event.ProductPurchasedEvent;
import nbc.chillguys.nebulazone.domain.product.service.ProductDomainService;
import nbc.chillguys.nebulazone.domain.transaction.dto.TransactionCreateCommand;
import nbc.chillguys.nebulazone.domain.transaction.entity.UserType;
import nbc.chillguys.nebulazone.domain.transaction.service.TransactionDomainService;
import nbc.chillguys.nebulazone.domain.user.entity.User;

@RequiredArgsConstructor
@Component
public class ProductPurchasedEventListener {

	private final ProductDomainService productDomainService;
	private final TransactionDomainService transactionDomainService;
	private final NotificationService notificationService;

	@Async
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handle(ProductPurchasedEvent event) {
		Product product = event.product();
		productDomainService.markProductAsPurchased(product.getId());

		String txMethod = product.getTxMethod().name();
		LocalDateTime purchasedAt = event.purchasedAt();
		Long price = product.getPrice();
		User buyer = event.buyer();
		TransactionCreateCommand buyerTxCreateCommand = TransactionCreateCommand.of(
			buyer, UserType.BUYER, product, txMethod, price, purchasedAt
		);
		transactionDomainService.createTransaction(buyerTxCreateCommand);

		TransactionCreateCommand sellerTxCreateCommand = TransactionCreateCommand.of(
			product.getSeller(), UserType.SELLER, product, txMethod, price, purchasedAt
		);
		transactionDomainService.createTransaction(sellerTxCreateCommand);

		notificationService.sendProductPurchaseNotification(
			product.getId(), product.getSellerId(), buyer.getId(), product.getName(), buyer.getNickname()
		);
	}
}
