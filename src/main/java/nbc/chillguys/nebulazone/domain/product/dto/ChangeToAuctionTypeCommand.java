package nbc.chillguys.nebulazone.domain.product.dto;

import nbc.chillguys.nebulazone.domain.product.entity.ProductEndTime;

public record ChangeToAuctionTypeCommand(
	Long productId,
	Long userId,
	Long catalogId,
	Long price,
	ProductEndTime endTime
) {
}
