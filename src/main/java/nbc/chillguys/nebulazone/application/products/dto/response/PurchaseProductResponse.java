package nbc.chillguys.nebulazone.application.products.dto.response;

import java.time.LocalDateTime;

import nbc.chillguys.nebulazone.domain.transaction.entity.Transaction;

public record PurchaseProductResponse(
	Long txId,
	LocalDateTime createdAt
) {

	public static PurchaseProductResponse from(Transaction tx) {
		return new PurchaseProductResponse(tx.getId(), tx.getCreatedAt());
	}
}
