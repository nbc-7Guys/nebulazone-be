package nbc.chillguys.nebulazone.application.transaction.dto.request;

import nbc.chillguys.nebulazone.domain.transaction.entity.TransactionMethod;

public record TransactionAdminSearchRequest(
	String keyword,
	TransactionMethod method,
	int page,
	int size
) {
}
