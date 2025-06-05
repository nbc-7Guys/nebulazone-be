package nbc.chillguys.nebulazone.application.products.dto.response;

public record DeleteProductResponse(
	Long productId
) {
	public static DeleteProductResponse from(Long productId) {
		return new DeleteProductResponse(productId);
	}
}
