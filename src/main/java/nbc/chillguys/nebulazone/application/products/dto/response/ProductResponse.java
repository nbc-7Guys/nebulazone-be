package nbc.chillguys.nebulazone.application.products.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

import nbc.chillguys.nebulazone.domain.products.entity.Product;
import nbc.chillguys.nebulazone.domain.products.entity.ProductEndTime;
import nbc.chillguys.nebulazone.domain.products.entity.ProductImage;
import nbc.chillguys.nebulazone.domain.products.entity.ProductTxMethod;

public record ProductResponse(

	Long productId,
	String productName,
	String productDescription,
	Long productPrice,
	ProductTxMethod productTxMethod,
	ProductEndTime endTime,
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	LocalDateTime createdAt,
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	LocalDateTime modifiedAt,
	List<String> productImageUrls) {

	public static ProductResponse from(Product product, ProductEndTime endTime) {
		return new ProductResponse(
			product.getId(),
			product.getName(),
			product.getDescription(),
			product.getPrice(),
			product.getTxMethod(),
			endTime,
			product.getCreatedAt(),
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
			product.getCreatedAt(),
			product.getModifiedAt(),
			product.getProductImages().stream()
				.map(ProductImage::getUrl)
				.toList()
		);
	}
}
