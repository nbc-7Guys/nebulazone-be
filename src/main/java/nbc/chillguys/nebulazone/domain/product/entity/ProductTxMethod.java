package nbc.chillguys.nebulazone.domain.product.entity;

import java.util.Arrays;

import nbc.chillguys.nebulazone.domain.product.exception.ProductErrorCode;
import nbc.chillguys.nebulazone.domain.product.exception.ProductException;

public enum ProductTxMethod {
	DIRECT, AUCTION;

	public static ProductTxMethod of(String txMethod) {
		return Arrays.stream(ProductTxMethod.values())
			.filter(method -> method.name().equalsIgnoreCase(txMethod))
			.findFirst()
			.orElseThrow(() -> new ProductException(ProductErrorCode.INVALID_PRODUCT_TYPE));
	}
}
