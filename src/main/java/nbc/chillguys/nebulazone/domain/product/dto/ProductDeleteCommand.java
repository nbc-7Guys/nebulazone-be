package nbc.chillguys.nebulazone.domain.product.dto;

import nbc.chillguys.nebulazone.domain.catalog.entity.Catalog;
import nbc.chillguys.nebulazone.domain.user.entity.User;

public record ProductDeleteCommand(
	User user,
	Catalog catalog,
	Long productId
) {

	public static ProductDeleteCommand of(User user, Catalog catalog, Long productId) {
		return new ProductDeleteCommand(user, catalog, productId);
	}
}
