package nbc.chillguys.nebulazone.domain.products.dto;

import nbc.chillguys.nebulazone.domain.catalog.entity.Catalog;
import nbc.chillguys.nebulazone.domain.user.entity.User;

public record ProductPurchaseCommand(
	User user,
	Catalog catalog,
	Long productId
) {

	public static ProductPurchaseCommand of(User user, Catalog catalog, Long productId) {
		return new ProductPurchaseCommand(user, catalog, productId);
	}
}
