package nbc.chillguys.nebulazone.domain.transaction.dto;

import nbc.chillguys.nebulazone.domain.transaction.entity.TransactionMethod;

public record TransactionAdminSearchQueryCommand(
	String keyword,
	TransactionMethod method
) {
}
