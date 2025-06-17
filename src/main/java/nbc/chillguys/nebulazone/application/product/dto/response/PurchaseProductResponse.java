package nbc.chillguys.nebulazone.application.product.dto.response;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import nbc.chillguys.nebulazone.domain.transaction.entity.Transaction;

public record PurchaseProductResponse(
	Long buyerTxId,

	Long sellerTxId,

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	LocalDateTime createdAt
) {

	public static PurchaseProductResponse from(Transaction buyerTx, Transaction sellerTx) {
		return new PurchaseProductResponse(buyerTx.getId(), sellerTx.getId(), buyerTx.getCreatedAt());
	}
}
