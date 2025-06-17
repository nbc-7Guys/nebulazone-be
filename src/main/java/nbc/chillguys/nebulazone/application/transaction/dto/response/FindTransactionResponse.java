package nbc.chillguys.nebulazone.application.transaction.dto.response;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import nbc.chillguys.nebulazone.domain.transaction.dto.TransactionFindAllInfo;
import nbc.chillguys.nebulazone.domain.transaction.entity.TransactionMethod;

public record FindTransactionResponse(
	Long txId,
	Long txPrice,
	TransactionMethod txMethod,
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	LocalDateTime txCreatedAt,
	String productName,
	boolean isSold,
	Long userId,
	String userNickname,
	String userType
) {

	public static FindTransactionResponse from(TransactionFindAllInfo txFindAllInfo) {
		return new FindTransactionResponse(
			txFindAllInfo.txId(),
			txFindAllInfo.txPrice(),
			txFindAllInfo.txMethod(),
			txFindAllInfo.txCreateAt(),
			txFindAllInfo.productName(),
			txFindAllInfo.isSold(),
			txFindAllInfo.userId(),
			txFindAllInfo.userNickname(),
			txFindAllInfo.userType().name()
		);
	}
}
