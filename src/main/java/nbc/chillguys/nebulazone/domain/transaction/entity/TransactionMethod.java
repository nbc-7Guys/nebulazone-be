package nbc.chillguys.nebulazone.domain.transaction.entity;

import java.util.Arrays;

import nbc.chillguys.nebulazone.domain.transaction.exception.TransactionErrorCode;
import nbc.chillguys.nebulazone.domain.transaction.exception.TransactionException;

public enum TransactionMethod {
	DIRECT, AUCTION;

	public static TransactionMethod of(String txMethod) {
		return Arrays.stream(TransactionMethod.values())
			.filter(method -> method.name().equalsIgnoreCase(txMethod))
			.findFirst()
			.orElseThrow(() -> new TransactionException(TransactionErrorCode.INVALID_TX_METHOD));
	}
}
