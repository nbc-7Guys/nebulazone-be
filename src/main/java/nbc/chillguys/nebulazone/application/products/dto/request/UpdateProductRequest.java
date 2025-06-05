package nbc.chillguys.nebulazone.application.products.dto.request;

import jakarta.validation.constraints.NotBlank;
import nbc.chillguys.nebulazone.domain.catalog.entity.Catalog;
import nbc.chillguys.nebulazone.domain.products.dto.ProductUpdateCommand;
import nbc.chillguys.nebulazone.domain.user.entity.User;

public record UpdateProductRequest(
	@NotBlank(message = "상품명을 입력해주세요.")
	String name,

	@NotBlank(message = "판매글 본문을 입력해주세요.")
	String description
) {

	public ProductUpdateCommand toCommand(User user, Catalog catalog, Long productId) {
		return new ProductUpdateCommand(user, catalog, productId, name, description);
	}
}
