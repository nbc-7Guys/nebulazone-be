package nbc.chillguys.nebulazone.domain.product.dto;

import nbc.chillguys.nebulazone.domain.product.entity.ProductTxMethod;

public record ProductAdminSearchQueryCommand(
	String keyword,
	ProductTxMethod txMethod,
	Boolean isSold
) {
}
