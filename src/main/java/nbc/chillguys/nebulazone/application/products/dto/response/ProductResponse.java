package nbc.chillguys.nebulazone.application.products.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import nbc.chillguys.nebulazone.domain.products.entity.Product;
import nbc.chillguys.nebulazone.domain.products.entity.ProductEndTime;
import nbc.chillguys.nebulazone.domain.products.entity.ProductImage;
import nbc.chillguys.nebulazone.domain.products.entity.ProductTxMethod;

public record ProductResponse(

	Long productId,
	String name,
	String description,
	Long price,
	ProductTxMethod txMethod,
	ProductEndTime endTime,
	LocalDateTime modifiedAt,
	List<String> imageUrls) {

	public static ProductResponse from(Product product, ProductEndTime endTime) {
		return new ProductResponse(
			product.getId(),
			product.getName(),
			product.getDescription(),
			product.getPrice(),
			product.getTxMethod(),
			endTime,
			product.getModifiedAt(),
			product.getProductImages().stream()
				.map(ProductImage::getUrl)
				.toList()
		);
	}

	public static ProductResponse from(Product product) {
		return new ProductResponse(
			product.getId(),
			product.getName(),
			product.getDescription(),
			product.getPrice(),
			product.getTxMethod(),
			null,
			product.getModifiedAt(),
			product.getProductImages().stream()
				.map(ProductImage::getUrl)
				.toList()
		);
	}
}
