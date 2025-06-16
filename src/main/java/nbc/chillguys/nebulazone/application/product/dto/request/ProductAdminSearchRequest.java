package nbc.chillguys.nebulazone.application.product.dto.request;

import nbc.chillguys.nebulazone.domain.product.entity.ProductTxMethod;

public record ProductAdminSearchRequest(
	String keyword,
	ProductTxMethod txMethod,
	Boolean isSold,
	int page,
	int size
) {
}
