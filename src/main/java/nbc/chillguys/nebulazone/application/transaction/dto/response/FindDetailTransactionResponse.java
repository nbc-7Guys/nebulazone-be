package nbc.chillguys.nebulazone.application.transaction.dto.response;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import nbc.chillguys.nebulazone.domain.transaction.dto.TransactionFindDetailInfo;
import nbc.chillguys.nebulazone.domain.transaction.entity.TransactionMethod;

public record FindDetailTransactionResponse(
	Long txId,
	Long sellerId,
	String sellerNickname,
	String sellerEmail,
	Long txPrice,
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	LocalDateTime txCreatedAt,
	TransactionMethod txMethod,
	Long productId,
	String productName,
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	LocalDateTime productCreatedAt,
	boolean isSold
) {

	public static FindDetailTransactionResponse from(TransactionFindDetailInfo detailInfo) {
		return new FindDetailTransactionResponse(
			detailInfo.txId(),
			detailInfo.sellerId(),
			detailInfo.sellerNickname(),
			detailInfo.sellerEmail(),
			detailInfo.txPrice(),
			detailInfo.txCreatedAt(),
			detailInfo.txMethod(),
			detailInfo.productId(),
			detailInfo.productName(),
			detailInfo.productCreatedAt(),
			detailInfo.isSold()
		);
	}
}
