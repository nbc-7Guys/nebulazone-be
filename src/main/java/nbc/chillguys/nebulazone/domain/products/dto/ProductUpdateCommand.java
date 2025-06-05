package nbc.chillguys.nebulazone.domain.products.dto;

import nbc.chillguys.nebulazone.domain.catalog.entity.Catalog;
import nbc.chillguys.nebulazone.domain.user.entity.User;

public record ProductUpdateCommand(
	User user,
	Catalog catalog,
	Long productId,
	String name,
	String description
) {
}
