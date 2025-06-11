package nbc.chillguys.nebulazone.application.products.dto.request;

import nbc.chillguys.nebulazone.domain.products.entity.ProductTxMethod;

public record AdminProductSearchRequest(
	String keyword,
	ProductTxMethod txMethod,
	Boolean isSold,
	int page,
	int size
) {
}
