package nbc.chillguys.nebulazone.application.products.dto.request;

import nbc.chillguys.nebulazone.domain.products.entity.ProductTxMethod;

public record ProductAdminSearchRequest(
	String keyword,
	ProductTxMethod txMethod,
	Boolean isSold,
	int page,
	int size
) {
}
