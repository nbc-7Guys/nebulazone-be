package nbc.chillguys.nebulazone.application.products.dto.request;

import java.util.List;

import nbc.chillguys.nebulazone.domain.products.entity.ProductTxMethod;

public record AdminProductUpdateRequest(
	String name,
	String description,
	Long price,
	ProductTxMethod txMethod,
	List<String> productImageUrls
) {
}
