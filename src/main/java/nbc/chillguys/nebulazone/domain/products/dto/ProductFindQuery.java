package nbc.chillguys.nebulazone.domain.products.dto;

public record ProductFindQuery(
	Long catalogId,
	Long productId
) {

	public static ProductFindQuery of(Long catalogId, Long productId) {
		return new ProductFindQuery(catalogId, productId);
	}
}
