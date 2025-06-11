package nbc.chillguys.nebulazone.application.transaction.dto.response;

import nbc.chillguys.nebulazone.domain.transaction.dto.AdminTransactionInfo;

public record AdminTransactionResponse(
	Long txId,
	Long price,
	String method,
	String userNickname,
	Long productId,
	String productName,
	java.time.LocalDateTime createdAt,
	java.time.LocalDateTime modifiedAt
) {
	public static AdminTransactionResponse from(AdminTransactionInfo info) {
		return new AdminTransactionResponse(
			info.txId(),
			info.price(),
			info.method(),
			info.userNickname(),
			info.productId(),
			info.productName(),
			info.createdAt(),
			info.modifiedAt()
		);
	}
}
