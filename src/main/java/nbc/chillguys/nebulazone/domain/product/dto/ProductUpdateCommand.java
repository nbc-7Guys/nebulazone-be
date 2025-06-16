package nbc.chillguys.nebulazone.domain.product.dto;

import java.util.List;

import nbc.chillguys.nebulazone.domain.catalog.entity.Catalog;
import nbc.chillguys.nebulazone.domain.user.entity.User;

public record ProductUpdateCommand(
	User user,
	Catalog catalog,
	Long productId,
	List<String> imageUrls,
	String name,
	String description
) {
}
