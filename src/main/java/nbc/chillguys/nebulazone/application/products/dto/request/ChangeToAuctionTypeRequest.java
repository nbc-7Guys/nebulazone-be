package nbc.chillguys.nebulazone.application.products.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import nbc.chillguys.nebulazone.domain.catalog.entity.Catalog;
import nbc.chillguys.nebulazone.domain.products.dto.ChangeToAuctionTypeCommand;
import nbc.chillguys.nebulazone.domain.products.entity.ProductEndTime;
import nbc.chillguys.nebulazone.domain.user.entity.User;

public record ChangeToAuctionTypeRequest(
	@NotNull(message = "시작 가격을 입력해주세요.")
	Long price,

	@NotEmpty(message = "마감 시간을 입력해주세요.")
	String endTime
) {

	public ProductEndTime getProductEndTime() {
		return ProductEndTime.of(endTime);
	}

	public ChangeToAuctionTypeCommand toCommand(User user, Catalog catalog, Long productId) {
		return new ChangeToAuctionTypeCommand(user, catalog, productId, price, getProductEndTime());
	}
}
