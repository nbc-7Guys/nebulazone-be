package nbc.chillguys.nebulazone.application.products.dto.request;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import nbc.chillguys.nebulazone.domain.catalog.entity.Catalog;
import nbc.chillguys.nebulazone.domain.products.dto.ProductUpdateCommand;
import nbc.chillguys.nebulazone.domain.user.entity.User;

public record UpdateProductRequest(
	@NotBlank(message = "상품명을 입력해주세요.")
	String name,

	@NotBlank(message = "판매글 본문을 입력해주세요.")
	String description,

	@NotNull(message = "유지할 이미지 주소 목록을 입력해주세요.")
	List<String> remainImageUrls
) {

	public ProductUpdateCommand toCommand(User user, Catalog catalog, Long productId, List<String> newImageUrls) {
		return new ProductUpdateCommand(user, catalog, productId, newImageUrls, name, description);
	}
}
