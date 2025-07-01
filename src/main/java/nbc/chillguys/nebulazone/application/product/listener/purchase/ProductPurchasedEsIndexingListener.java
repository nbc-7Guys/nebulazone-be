package nbc.chillguys.nebulazone.application.product.listener.purchase;

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
import nbc.chillguys.nebulazone.domain.product.service.ProductDomainService;

@Slf4j
@RequiredArgsConstructor
@Component
public class ProductPurchasedEsIndexingListener {

	private final ProductDomainService productDomainService;

	@Async
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	@Retryable(
		backoff = @Backoff(delay = 2000),
		retryFor = Exception.class
	)
	public void handle(ProductPurchasedEvent event) {
		Product product = event.product();

		productDomainService.markProductAsPurchased(product.getId());
	}

	@Recover
	public void recover(Exception ex, ProductPurchasedEvent event) {
		log.error("구매 후처리 - ES 반영 실패: {}", event, ex);
	}
}
