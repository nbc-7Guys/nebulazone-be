package nbc.chillguys.nebulazone.application.product.dto.request;

import java.util.List;

import nbc.chillguys.nebulazone.domain.product.entity.ProductTxMethod;

public record ProductAdminUpdateRequest(
	String name,
	String description,
	Long price,
	ProductTxMethod txMethod,
	List<String> productImageUrls
) {
}
