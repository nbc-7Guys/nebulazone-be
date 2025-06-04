package nbc.chillguys.nebulazone.domain.products.dto;

import nbc.chillguys.nebulazone.domain.auction.entity.Auction;
import nbc.chillguys.nebulazone.domain.catalog.entity.Catalog;
import nbc.chillguys.nebulazone.domain.user.entity.User;

public record ProductDeleteCommand(
	User user,
	Catalog catalog,
	Auction auction,
	Long productId
) {

	public static ProductDeleteCommand of(User user, Catalog catalog, Auction auction, Long productId) {
		return new ProductDeleteCommand(user, catalog, auction, productId);
	}
}
