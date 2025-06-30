package nbc.chillguys.nebulazone.application.product.dto.request;

import nbc.chillguys.nebulazone.domain.product.entity.ProductTxMethod;

public record ProductAdminUpdateRequest(
	String name,
	String description,
	Long price,
	ProductTxMethod txMethod
) {
}
