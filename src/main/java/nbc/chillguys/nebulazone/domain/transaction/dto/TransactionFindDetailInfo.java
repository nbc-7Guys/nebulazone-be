package nbc.chillguys.nebulazone.domain.transaction.dto;

import java.time.LocalDateTime;

import com.querydsl.core.annotations.QueryProjection;

import nbc.chillguys.nebulazone.domain.transaction.entity.TransactionMethod;

public record TransactionFindDetailInfo(
	Long txId,
	Long sellerId,
	String sellerNickname,
	String sellerEmail,
	Long txPrice,
	LocalDateTime txCreatedAt,
	TransactionMethod txMethod,
	Long productId,
	String productName,
	LocalDateTime productCreatedAt,
	boolean isSold
) {

	@QueryProjection
	public TransactionFindDetailInfo {
	}
}
