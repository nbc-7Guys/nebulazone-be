package nbc.chillguys.nebulazone.domain.auction.dto;

import java.time.LocalDateTime;

import nbc.chillguys.nebulazone.domain.product.entity.Product;
import nbc.chillguys.nebulazone.domain.product.entity.ProductEndTime;

public record AuctionCreateCommand(
	Product product,
	LocalDateTime endTime
) {

	public static AuctionCreateCommand of(Product product, ProductEndTime endTime) {
		return new AuctionCreateCommand(
			product,
			LocalDateTime.now().plusSeconds(endTime.getSeconds())
		);
	}
}
