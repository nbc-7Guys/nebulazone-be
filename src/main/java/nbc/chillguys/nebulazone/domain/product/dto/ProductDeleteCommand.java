package nbc.chillguys.nebulazone.domain.product.dto;

public record ProductDeleteCommand(
	Long productId,
	Long userId,
	Long catalogId
) {

	public static ProductDeleteCommand of(Long productId, Long userId, Long catalogId) {
		return new ProductDeleteCommand(productId, userId, catalogId);
	}
}
