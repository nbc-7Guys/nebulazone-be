package nbc.chillguys.nebulazone.domain.transaction.dto;

import nbc.chillguys.nebulazone.domain.transaction.entity.TransactionMethod;

public record AdminTransactionSearchQueryCommand(
	String keyword,
	TransactionMethod method
) {
}
