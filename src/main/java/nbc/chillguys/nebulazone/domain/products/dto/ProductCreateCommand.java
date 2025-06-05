package nbc.chillguys.nebulazone.domain.products.dto;

import nbc.chillguys.nebulazone.application.products.dto.request.CreateProductRequest;
import nbc.chillguys.nebulazone.domain.catalog.entity.Catalog;
import nbc.chillguys.nebulazone.domain.products.entity.ProductTxMethod;
import nbc.chillguys.nebulazone.domain.user.entity.User;

public record ProductCreateCommand(
	User user,
	Catalog catalog,
	String name,
	String description,
	Long price,
	ProductTxMethod txMethod
) {

	public static ProductCreateCommand of(User user, Catalog catalog, CreateProductRequest request) {
		return new ProductCreateCommand(
			user,
			catalog,
			request.name(),
			request.description(),
			request.price(),
			request.getProductTxMethod()
		);
	}
}
