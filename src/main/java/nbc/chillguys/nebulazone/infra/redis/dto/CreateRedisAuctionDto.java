package nbc.chillguys.nebulazone.infra.redis.dto;

import nbc.chillguys.nebulazone.domain.auction.entity.Auction;
import nbc.chillguys.nebulazone.domain.product.entity.Product;
import nbc.chillguys.nebulazone.domain.product.entity.ProductEndTime;
import nbc.chillguys.nebulazone.domain.user.entity.User;

public record CreateRedisAuctionDto(
	Product product,
	Auction auction,
	User user,
	ProductEndTime ProductEndTime
) {
	public static CreateRedisAuctionDto of(Product product, Auction auction, User user, ProductEndTime productEndTime) {
		return new CreateRedisAuctionDto(
			product,
			auction,
			user,
			productEndTime
		);
	}
}
