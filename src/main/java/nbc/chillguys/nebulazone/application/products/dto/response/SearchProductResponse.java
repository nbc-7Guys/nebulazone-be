package nbc.chillguys.nebulazone.application.products.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

import nbc.chillguys.nebulazone.domain.products.vo.ProductDocument;

public record SearchProductResponse(
	Long productId,
	String productName,
	Long productPrice,
	String txMethod,
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	LocalDateTime createdAt,
	List<String> productImages
) {
	public static SearchProductResponse from(ProductDocument productDocument) {
		return new SearchProductResponse(
			productDocument.productId(),
			productDocument.name(),
			productDocument.price(),
			productDocument.txMethod(),
			productDocument.createdAt(),
			productDocument.imageUrls()
		);
	}
}
