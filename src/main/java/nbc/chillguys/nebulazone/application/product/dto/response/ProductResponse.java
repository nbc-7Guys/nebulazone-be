package nbc.chillguys.nebulazone.application.product.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

import nbc.chillguys.nebulazone.domain.product.entity.Product;
import nbc.chillguys.nebulazone.domain.product.entity.ProductEndTime;
import nbc.chillguys.nebulazone.domain.product.entity.ProductImage;
import nbc.chillguys.nebulazone.domain.product.entity.ProductTxMethod;

public record ProductResponse(

	Long productId,
	String productName,
	String productDescription,
	Long productPrice,
	ProductTxMethod productTxMethod,
	boolean isSold,
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
			product.isSold(),
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
			product.isSold(),
			null,
			product.getCreatedAt(),
			product.getModifiedAt(),
			product.getProductImages().stream()
				.map(ProductImage::getUrl)
				.toList()
		);
	}
}
