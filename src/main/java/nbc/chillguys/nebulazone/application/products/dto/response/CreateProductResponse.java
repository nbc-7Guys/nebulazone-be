package nbc.chillguys.nebulazone.application.products.dto.response;

import java.time.LocalDateTime;
import java.util.List;

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
	LocalDateTime modifiedAt,
	List<String> imageUrls) {

	public static CreateProductResponse from(Product product, List<String> imageUrls) {
		return CreateProductResponse.builder()
			.name(product.getName())
			.description(product.getDescription())
			.price(product.getPrice())
			.txMethod(product.getTxMethod())
			.modifiedAt(product.getModifiedAt())
			.imageUrls(imageUrls)
			.build();
	}

}
