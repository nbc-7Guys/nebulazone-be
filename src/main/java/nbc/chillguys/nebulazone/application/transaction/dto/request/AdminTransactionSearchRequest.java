package nbc.chillguys.nebulazone.application.transaction.dto.request;

import nbc.chillguys.nebulazone.domain.transaction.entity.TransactionMethod;

public record AdminTransactionSearchRequest(
	String keyword,
	TransactionMethod method,
	int page,
	int size
) {
}
