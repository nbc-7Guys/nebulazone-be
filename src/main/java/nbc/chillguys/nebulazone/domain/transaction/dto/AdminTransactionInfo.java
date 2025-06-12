package nbc.chillguys.nebulazone.domain.transaction.dto;

import nbc.chillguys.nebulazone.domain.transaction.entity.Transaction;

public record AdminTransactionInfo(
	Long txId,
	Long price,
	String method,
	String userNickname,
	Long productId,
	String productName,
	java.time.LocalDateTime createdAt,
	java.time.LocalDateTime modifiedAt
) {
	public static AdminTransactionInfo from(Transaction tx) {
		return new AdminTransactionInfo(
			tx.getId(),
			tx.getPrice(),
			tx.getMethod().name(),
			tx.getUser().getNickname(),
			tx.getProduct().getId(),
			tx.getProduct().getName(),
			tx.getCreatedAt(),
			tx.getModifiedAt()
		);
	}
}
