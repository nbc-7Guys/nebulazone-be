package nbc.chillguys.nebulazone.application.product.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import nbc.chillguys.nebulazone.domain.product.dto.ChangeToAuctionTypeCommand;
import nbc.chillguys.nebulazone.domain.product.entity.ProductEndTime;

public record ChangeToAuctionTypeRequest(
	@NotNull(message = "시작 가격을 입력해주세요.")
	Long price,

	@NotEmpty(message = "마감 시간을 입력해주세요.")
	String endTime
) {

	public ProductEndTime getProductEndTime() {
		return ProductEndTime.of(endTime);
	}

	public ChangeToAuctionTypeCommand toCommand(Long productId, Long userId, Long catalogId) {
		return new ChangeToAuctionTypeCommand(productId, userId, catalogId, price, getProductEndTime());
	}
}
