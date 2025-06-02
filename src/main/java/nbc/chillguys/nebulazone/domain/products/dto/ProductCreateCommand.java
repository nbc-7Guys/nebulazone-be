package nbc.chillguys.nebulazone.domain.products.dto;

import lombok.Builder;
import nbc.chillguys.nebulazone.application.products.dto.request.CreateProductRequest;
import nbc.chillguys.nebulazone.domain.catalog.entity.Catalog;
import nbc.chillguys.nebulazone.domain.products.entity.ProductEndTime;
import nbc.chillguys.nebulazone.domain.products.entity.ProductTxMethod;
import nbc.chillguys.nebulazone.domain.user.entity.User;

@Builder
public record ProductCreateCommand(
	User user,
	Catalog catalog,
	String name,
	String description,
	Long price,
	ProductTxMethod txMethod,
	ProductEndTime endTime
) {

	public static ProductCreateCommand of(User user, Catalog catalog, CreateProductRequest request) {
		return ProductCreateCommand.builder()
			.user(user)
			.catalog(catalog)
			.name(request.name())
			.description(request.description())
			.price(request.price())
			.txMethod(request.getProductTxMethod())
			.endTime(request.getProductEndTime())
			.build();
	}
}
