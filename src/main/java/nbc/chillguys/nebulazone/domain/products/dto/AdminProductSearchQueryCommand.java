package nbc.chillguys.nebulazone.domain.products.dto;

import nbc.chillguys.nebulazone.domain.products.entity.ProductTxMethod;

public record AdminProductSearchQueryCommand(
	String keyword,
	ProductTxMethod txMethod,
	Boolean isSold
) {
}
