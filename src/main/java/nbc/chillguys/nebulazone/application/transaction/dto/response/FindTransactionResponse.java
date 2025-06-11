package nbc.chillguys.nebulazone.application.transaction.dto.response;

import java.time.LocalDateTime;

import nbc.chillguys.nebulazone.domain.transaction.dto.TransactionFindAllInfo;
import nbc.chillguys.nebulazone.domain.transaction.entity.TransactionMethod;

public record FindTransactionResponse(
	Long txId,
	Long txPrice,
	TransactionMethod txMethod,
	LocalDateTime txCreateAt,
	String productName,
	boolean isSold
) {

	public static FindTransactionResponse from(TransactionFindAllInfo txFindAllInfo) {
		return new FindTransactionResponse(
			txFindAllInfo.txId(),
			txFindAllInfo.txPrice(),
			txFindAllInfo.txMethod(),
			txFindAllInfo.txCreateAt(),
			txFindAllInfo.productName(),
			txFindAllInfo.isSold()
		);
	}
}
