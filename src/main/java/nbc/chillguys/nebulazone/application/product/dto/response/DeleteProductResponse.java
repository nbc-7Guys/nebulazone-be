package nbc.chillguys.nebulazone.application.product.dto.response;

public record DeleteProductResponse(
	Long productId
) {
	public static DeleteProductResponse from(Long productId) {
		return new DeleteProductResponse(productId);
	}
}
