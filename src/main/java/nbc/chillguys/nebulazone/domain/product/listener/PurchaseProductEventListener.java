package nbc.chillguys.nebulazone.domain.product.listener;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.domain.product.entity.Product;
import nbc.chillguys.nebulazone.domain.product.event.PurchaseProductEvent;
import nbc.chillguys.nebulazone.domain.product.service.ProductDomainService;
import nbc.chillguys.nebulazone.domain.transaction.dto.TransactionCreateCommand;
import nbc.chillguys.nebulazone.domain.transaction.entity.UserType;
import nbc.chillguys.nebulazone.domain.transaction.service.TransactionDomainService;

@RequiredArgsConstructor
@Component
public class PurchaseProductEventListener {

	private final ProductDomainService productDomainService;
	private final TransactionDomainService transactionDomainService;

	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handlePurchaseProduct(PurchaseProductEvent event) {
		Product product = event.product();
		productDomainService.markProductAsPurchased(product.getId());

		String txMethod = product.getTxMethod().name();
		LocalDateTime purchasedAt = event.purchasedAt();
		Long price = product.getPrice();
		TransactionCreateCommand buyerTxCreateCommand = TransactionCreateCommand.of(
			event.buyer(), UserType.BUYER, product, txMethod, price, purchasedAt
		);
		transactionDomainService.createTransaction(buyerTxCreateCommand);

		TransactionCreateCommand sellerTxCreateCommand = TransactionCreateCommand.of(
			product.getSeller(), UserType.SELLER, product, txMethod, price, purchasedAt
		);
		transactionDomainService.createTransaction(sellerTxCreateCommand);
	}
}
