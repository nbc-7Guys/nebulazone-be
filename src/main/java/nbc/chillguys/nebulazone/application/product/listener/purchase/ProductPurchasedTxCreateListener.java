package nbc.chillguys.nebulazone.application.product.listener.purchase;

import java.time.LocalDateTime;

import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nbc.chillguys.nebulazone.domain.product.entity.Product;
import nbc.chillguys.nebulazone.domain.product.event.ProductPurchasedEvent;
import nbc.chillguys.nebulazone.domain.transaction.dto.TransactionCreateCommand;
import nbc.chillguys.nebulazone.domain.transaction.entity.UserType;
import nbc.chillguys.nebulazone.domain.transaction.service.TransactionDomainService;
import nbc.chillguys.nebulazone.domain.user.entity.User;

@Slf4j
@RequiredArgsConstructor
@Component
public class ProductPurchasedTxCreateListener {

	private final TransactionDomainService transactionDomainService;

	@Async
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	@Retryable(
		backoff = @Backoff(delay = 2000),
		retryFor = Exception.class
	)
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

	@Recover
	public void recover(Exception ex, ProductPurchasedEvent event) {
		log.error("구매 후처리 - 거래내역 생성 실패: {}", event, ex);
	}
}
