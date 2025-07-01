package nbc.chillguys.nebulazone.application.product.listener.purchase;

import java.time.LocalDateTime;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.domain.product.entity.Product;
import nbc.chillguys.nebulazone.domain.product.event.ProductPurchasedEvent;
import nbc.chillguys.nebulazone.domain.transaction.dto.TransactionCreateCommand;
import nbc.chillguys.nebulazone.domain.transaction.entity.UserType;
import nbc.chillguys.nebulazone.domain.transaction.service.TransactionDomainService;
import nbc.chillguys.nebulazone.domain.user.entity.User;

@RequiredArgsConstructor
@Component
public class ProductPurchasedTxCreateListener {

	private final TransactionDomainService transactionDomainService;

	@Async
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handle(ProductPurchasedEvent event) {
		Product product = event.product();
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
	}
}
