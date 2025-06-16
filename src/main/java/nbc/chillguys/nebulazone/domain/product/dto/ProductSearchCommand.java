package nbc.chillguys.nebulazone.domain.product.dto;

import nbc.chillguys.nebulazone.domain.product.entity.ProductTxMethod;

public record ProductSearchCommand(
	String productName,
	String sellerNickname,
	String txMethod,
	Long priceFrom,
	Long priceTo,
	int page,
	int size
) {
	public static ProductSearchCommand of(String productName, String sellerNickname, ProductTxMethod txMethod,
		Long priceFrom, Long priceTo,
		int page, int size) {
		return new ProductSearchCommand(
			productName,
			sellerNickname,
			txMethod.name(),
			priceFrom,
			priceTo,
			page,
			size);
	}
}
