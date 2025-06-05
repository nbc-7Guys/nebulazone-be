package nbc.chillguys.nebulazone.domain.auction.dto;

import java.time.LocalDateTime;

import lombok.Builder;
import nbc.chillguys.nebulazone.domain.products.dto.ProductCreateCommand;
import nbc.chillguys.nebulazone.domain.products.entity.Product;

@Builder
public record AuctionCreateCommand(
	Product product,
	Long startPrice,
	LocalDateTime endTime
) {

	public static AuctionCreateCommand of(Product product, ProductCreateCommand command) {

		return AuctionCreateCommand.builder()
			.product(product)
			.startPrice(command.price())
			.endTime(LocalDateTime.now().plusSeconds(command.endTime().getSeconds()))
			.build();
	}
}
