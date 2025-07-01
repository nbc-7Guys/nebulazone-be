package nbc.chillguys.nebulazone.application.product.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import nbc.chillguys.nebulazone.domain.product.entity.ProductEndTime;
import nbc.chillguys.nebulazone.domain.product.entity.ProductTxMethod;

public record CreateProductRequest(

	@NotBlank(message = "상품명은 필수 입력값 입니다.")
	String name,

	@NotBlank(message = "판매글 본문은 필수 입력값 입니다.")
	String description,

	@NotNull(message = "판매 유형은 꼭 선택해주셔야 합니다.")
	String type,

	@NotNull(message = "판매 가격은 필수 입력값 입니다.")
	Long price,

	String endTime) {

	public ProductTxMethod getProductTxMethod() {
		return ProductTxMethod.of(type);
	}

	public ProductEndTime getProductEndTime() {
		return ProductEndTime.from(endTime);
	}

}
