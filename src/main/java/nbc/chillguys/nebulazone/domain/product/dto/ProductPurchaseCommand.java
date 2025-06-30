package nbc.chillguys.nebulazone.domain.product.dto;

public record ProductPurchaseCommand(
	Long productId,
	Long userId,
	Long catalogId
) {

	public static ProductPurchaseCommand of(Long productId, Long userId, Long catalogId) {
		return new ProductPurchaseCommand(productId, userId, catalogId);
	}
}
