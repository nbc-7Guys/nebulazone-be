package nbc.chillguys.nebulazone.application.product.dto.response;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import nbc.chillguys.nebulazone.domain.product.entity.Product;

public record PurchaseProductResponse(
	Long productId,
	Long price,

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	LocalDateTime purchasedAt
) {

	public static PurchaseProductResponse from(Product product, LocalDateTime purchasedAt) {
		return new PurchaseProductResponse(product.getId(), product.getPrice(), purchasedAt);
	}
}
