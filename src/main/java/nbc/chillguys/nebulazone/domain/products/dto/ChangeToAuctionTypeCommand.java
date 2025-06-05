package nbc.chillguys.nebulazone.domain.products.dto;

import nbc.chillguys.nebulazone.domain.catalog.entity.Catalog;
import nbc.chillguys.nebulazone.domain.products.entity.ProductEndTime;
import nbc.chillguys.nebulazone.domain.user.entity.User;

public record ChangeToAuctionTypeCommand(
	User user,
	Catalog catalog,
	Long productId,
	Long price,
	ProductEndTime endTime
) {
}
