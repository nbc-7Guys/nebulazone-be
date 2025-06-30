package nbc.chillguys.nebulazone.domain.product.dto;

public record ProductUpdateCommand(
	Long productId,
	Long userId,
	Long catalogId,
	String name,
	String description
) {
}
