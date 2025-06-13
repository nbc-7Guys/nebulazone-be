package nbc.chillguys.nebulazone.domain.transaction.dto;

import java.time.LocalDateTime;

import nbc.chillguys.nebulazone.domain.transaction.entity.Transaction;

public record TransactionAdminInfo(
	Long txId,
	Long price,
	String method,
	String userNickname,
	Long productId,
	String productName,
	LocalDateTime createdAt,
	LocalDateTime modifiedAt
) {
	public static TransactionAdminInfo from(Transaction tx) {
		return new TransactionAdminInfo(
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
