package nbc.chillguys.nebulazone.application.product.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

import nbc.chillguys.nebulazone.domain.product.vo.ProductDocument;

public record SearchProductResponse(
	Long productId,
	String productName,
	Long categoryId,
	Long sellerId,
	String sellerNickname,
	Long auctionId,
	boolean isSold,
	Long productPrice,
	String txMethod,
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	LocalDateTime createdAt,
	List<String> productImages
) {
	public static SearchProductResponse from(ProductDocument productDocument) {
		return new SearchProductResponse(
			productDocument.productId(),
			productDocument.productName(),
			productDocument.catalogId(),
			productDocument.sellerId(),
			productDocument.sellerNickname(),
			productDocument.auctionId(),
			productDocument.isSold(),
			productDocument.price(),
			productDocument.txMethod(),
			productDocument.createdAt(),
			productDocument.imageUrls()
		);
	}
}
