package nbc.chillguys.nebulazone.domain.products.entity;

import java.util.Arrays;

import nbc.chillguys.nebulazone.domain.products.exception.ProductErrorCode;
import nbc.chillguys.nebulazone.domain.products.exception.ProductException;

public enum ProductTxMethod {
	DIRECT, AUCTION;

	public static ProductTxMethod of(String txMethod) {
		return Arrays.stream(ProductTxMethod.values())
			.filter(method -> method.name().equalsIgnoreCase(txMethod))
			.findFirst()
			.orElseThrow(() -> new ProductException(ProductErrorCode.INVALID_PRODUCT_TYPE));
	}
}
