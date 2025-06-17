package nbc.chillguys.nebulazone.domain.transaction.dto;

import java.time.LocalDateTime;

import com.querydsl.core.annotations.QueryProjection;

import nbc.chillguys.nebulazone.domain.transaction.entity.TransactionMethod;
import nbc.chillguys.nebulazone.domain.transaction.entity.UserType;

public record TransactionFindAllInfo(
	Long txId,
	Long txPrice,
	TransactionMethod txMethod,
	LocalDateTime txCreateAt,
	String productName,
	boolean isSold,
	String userNickname,
	UserType userType
) {

	@QueryProjection
	public TransactionFindAllInfo {
	}
}
