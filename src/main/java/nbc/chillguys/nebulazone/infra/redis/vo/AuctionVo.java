package nbc.chillguys.nebulazone.infra.redis.vo;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

import nbc.chillguys.nebulazone.domain.auction.entity.Auction;
import nbc.chillguys.nebulazone.domain.auction.exception.AuctionErrorCode;
import nbc.chillguys.nebulazone.domain.auction.exception.AuctionException;
import nbc.chillguys.nebulazone.domain.bid.exception.BidErrorCode;
import nbc.chillguys.nebulazone.domain.bid.exception.BidException;
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

	public void validateAuctionNotClosed() {
		if (Duration.between(LocalDateTime.now(), this.endTime()).isNegative()) {
			throw new AuctionException(AuctionErrorCode.ALREADY_CLOSED_AUCTION);
		}
	}

	public void validateNotAuctionOwner(Long bidUserId) {
		if (Objects.equals(this.sellerId, bidUserId)) {
			throw new BidException(BidErrorCode.CANNOT_BID_OWN_AUCTION);
		}
	}

	public void validateWonAuction() {
		if (isWon) {
			throw new AuctionException(AuctionErrorCode.ALREADY_WON_AUCTION);
		}
	}

	public void validateMinimumBidPrice(Long bidPrice) {
		if (this.currentPrice == null) {
			if (this.startPrice > bidPrice) {
				throw new BidException(BidErrorCode.BID_PRICE_TOO_LOW_START_PRICE);
			}
		} else {
			if (this.currentPrice >= bidPrice) {
				throw new BidException(BidErrorCode.BID_PRICE_TOO_LOW_CURRENT_PRICE);
			}
		}
	}

}
