package nbc.chillguys.nebulazone.application.product.listener;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.domain.product.event.ProductUpdatedEvent;
import nbc.chillguys.nebulazone.domain.product.service.ProductDomainService;

@RequiredArgsConstructor
@Component
public class ProductUpdatedEventListener {

	private final ProductDomainService productDomainService;

	@Async
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handle(ProductUpdatedEvent event) {
		productDomainService.saveProductToEs(event.product());
	}
}
