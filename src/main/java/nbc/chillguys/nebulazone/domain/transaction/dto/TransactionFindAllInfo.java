package nbc.chillguys.nebulazone.domain.transaction.dto;

import java.time.LocalDateTime;

import com.querydsl.core.annotations.QueryProjection;

import nbc.chillguys.nebulazone.domain.transaction.entity.TransactionMethod;

public record TransactionFindAllInfo(
	Long txId,
	Long txPrice,
	TransactionMethod txMethod,
	LocalDateTime txCreateAt,
	String productName,
	boolean isSold
) {

	@QueryProjection
	public TransactionFindAllInfo {
	}
}
