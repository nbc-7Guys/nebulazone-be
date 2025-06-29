package nbc.chillguys.nebulazone.application.transaction.dto.response;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import nbc.chillguys.nebulazone.domain.transaction.dto.TransactionAdminInfo;

public record TransactionAdminResponse(
	Long txId,
	Long price,
	String method,
	String userNickname,
	Long productId,
	String productName,
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	LocalDateTime createdAt

) {
	public static TransactionAdminResponse from(TransactionAdminInfo info) {
		return new TransactionAdminResponse(
			info.txId(),
			info.price(),
			info.method(),
			info.userNickname(),
			info.productId(),
			info.productName(),
			info.createdAt()
		);
	}
}
