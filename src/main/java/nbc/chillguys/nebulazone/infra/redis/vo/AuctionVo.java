package nbc.chillguys.nebulazone.infra.redis.vo;

import java.time.LocalDateTime;

import nbc.chillguys.nebulazone.domain.auction.entity.Auction;
import nbc.chillguys.nebulazone.domain.product.entity.Product;
import nbc.chillguys.nebulazone.domain.user.entity.User;

public record AuctionVo(
	Long auctionId,
	Long startPrice,
	Long currentPrice,
	LocalDateTime endTime,
	boolean isWon,
	LocalDateTime createAt,
	Long productId,
	String productName,
	boolean isSold,
	Long sellerId,
	String sellerNickname,
	String sellerEmail
) {

	public static AuctionVo of(Product product, Auction auction, User user) {
		return new AuctionVo(
			auction.getId(),
			auction.getStartPrice(),
			auction.getCurrentPrice(),
			auction.getEndTime(),
			auction.isWon(),
			auction.getCreatedAt(),
			product.getId(),
			product.getName(),
			product.isSold(),
			user.getId(),
			user.getNickname(),
			user.getNickname()
		);
	}
}
