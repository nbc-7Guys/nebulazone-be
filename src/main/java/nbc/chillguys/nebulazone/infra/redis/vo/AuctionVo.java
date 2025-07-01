package nbc.chillguys.nebulazone.infra.redis.vo;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import lombok.AllArgsConstructor;
import lombok.Getter;
import nbc.chillguys.nebulazone.domain.auction.dto.AuctionAdminUpdateCommand;
import nbc.chillguys.nebulazone.domain.auction.entity.Auction;
import nbc.chillguys.nebulazone.domain.auction.exception.AuctionErrorCode;
import nbc.chillguys.nebulazone.domain.auction.exception.AuctionException;
import nbc.chillguys.nebulazone.domain.bid.exception.BidErrorCode;
import nbc.chillguys.nebulazone.domain.bid.exception.BidException;
import nbc.chillguys.nebulazone.domain.product.entity.Product;
import nbc.chillguys.nebulazone.domain.product.entity.ProductImage;
import nbc.chillguys.nebulazone.domain.user.entity.User;

@Getter
@AllArgsConstructor
public class AuctionVo {
	private Long auctionId;
	private Long startPrice;
	private Long currentPrice;
	private LocalDateTime endTime;
	private boolean isWon;
	private LocalDateTime createAt;
	private Long productId;
	private String productName;
	private boolean isSold;
	private Long sellerId;
	private String sellerNickname;
	private String sellerEmail;
	private List<String> productImageUrls;

	public static AuctionVo of(Product product, Auction auction, User user, List<String> productImageUrls) {
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
			user.getEmail(),
			productImageUrls
		);
	}

	public static AuctionVo fromAuction(Auction auction) {
		Product product = auction.getProduct();
		User seller = product.getSeller();

		List<String> imageUrls = product.getProductImages().stream()
			.map(ProductImage::getUrl)
			.toList();

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
			seller.getId(),
			seller.getNickname(),
			seller.getEmail(),
			imageUrls
		);
	}

	public void validAuctionNotClosed() {
		if (Duration.between(LocalDateTime.now(), this.endTime).isNegative()) {
			throw new AuctionException(AuctionErrorCode.ALREADY_CLOSED_AUCTION);
		}
	}

	public void validAuctionOwnerNotBid(Long bidUserId) {
		if (Objects.equals(this.sellerId, bidUserId)) {
			throw new BidException(BidErrorCode.CANNOT_BID_OWN_AUCTION);
		}
	}

	public void validWonAuction() {
		if (isWon) {
			throw new AuctionException(AuctionErrorCode.ALREADY_WON_AUCTION);
		}
	}

	public void validMinimumBidPrice(Long bidPrice) {
		if (this.currentPrice == 0L) {
			if (this.startPrice > bidPrice) {
				throw new BidException(BidErrorCode.BID_PRICE_TOO_LOW_START_PRICE);
			}
		} else {
			if (this.currentPrice >= bidPrice) {
				throw new BidException(BidErrorCode.BID_PRICE_TOO_LOW_CURRENT_PRICE);
			}
		}
	}

	public void validBidCancelBefore30Minutes() {
		if (this.endTime.minusMinutes(30).isBefore(LocalDateTime.now())) {
			throw new BidException(BidErrorCode.BID_CANCEL_TIME_LIMIT_EXCEEDED);
		}
	}

	public void validMismatchBidPrice(Long bidPrice) {
		if (!Objects.equals(currentPrice, bidPrice)) {
			throw new AuctionException(AuctionErrorCode.MISMATCH_BID_PRICE);
		}
	}

	public void validNotAuctionOwner(User loginUser) {
		if (!Objects.equals(this.sellerId, loginUser.getId())) {
			throw new AuctionException(AuctionErrorCode.AUCTION_NOT_OWNER);
		}
	}

	public void validateUpdatableByAdmin(AuctionAdminUpdateCommand command) {
		validAuctionNotClosed();

		validWonAuction();

		if (command.startPrice() != null && command.startPrice() <= 0) {
			throw new AuctionException(AuctionErrorCode.INVALID_START_PRICE);
		}
		if (command.currentPrice() != null && command.currentPrice() < 0) {
			throw new AuctionException(AuctionErrorCode.INVALID_CURRENT_PRICE);
		}
		if (command.endTime() != null && command.endTime().isBefore(LocalDateTime.now())) {
			throw new AuctionException(AuctionErrorCode.AUCTION_END_TIME_INVALID);
		}
	}

	public void updateByAdmin(Long startPrice, Long currentPrice, LocalDateTime endTime) {
		if (startPrice != null) {
			this.startPrice = startPrice;
		}
		if (currentPrice != null) {
			this.currentPrice = currentPrice;
		}
		if (endTime != null) {
			this.endTime = endTime;
		}
	}

}
