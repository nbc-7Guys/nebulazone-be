package nbc.chillguys.nebulazone.application.products.dto.response;

import java.time.LocalDateTime;

import lombok.Builder;
import nbc.chillguys.nebulazone.domain.products.entity.Product;
import nbc.chillguys.nebulazone.domain.products.entity.ProductEndTime;
import nbc.chillguys.nebulazone.domain.products.entity.ProductTxMethod;

@Builder
public record CreateProductResponse(

	String name,
	String description,
	Long price,
	ProductTxMethod txMethod,
	ProductEndTime endTime,
	LocalDateTime modifiedAt) {

	public static CreateProductResponse from(Product product) {
		return CreateProductResponse.builder()
			.name(product.getName())
			.description(product.getDescription())
			.price(product.getPrice())
			.txMethod(product.getTxMethod())
			.endTime(product.getEndTime())
			.modifiedAt(product.getModifiedAt())
			.build();
	}

}
