package nbc.chillguys.nebulazone.application.product.dto.request;

import jakarta.validation.constraints.NotBlank;
import nbc.chillguys.nebulazone.domain.product.dto.ProductUpdateCommand;

public record UpdateProductRequest(
	@NotBlank(message = "상품명을 입력해주세요.")
	String name,

	@NotBlank(message = "판매글 본문을 입력해주세요.")
	String description
) {

	public ProductUpdateCommand toCommand(Long productId, Long userId, Long catalogId) {
		return new ProductUpdateCommand(productId, userId, catalogId, name, description);
	}
}
