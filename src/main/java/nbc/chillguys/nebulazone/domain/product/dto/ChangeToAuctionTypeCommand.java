package nbc.chillguys.nebulazone.domain.product.dto;

import nbc.chillguys.nebulazone.domain.catalog.entity.Catalog;
import nbc.chillguys.nebulazone.domain.product.entity.ProductEndTime;
import nbc.chillguys.nebulazone.domain.user.entity.User;

public record ChangeToAuctionTypeCommand(
	User user,
	Catalog catalog,
	Long productId,
	Long price,
	ProductEndTime endTime
) {
}
