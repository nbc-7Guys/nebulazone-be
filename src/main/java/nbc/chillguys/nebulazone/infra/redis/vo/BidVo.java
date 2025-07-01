package nbc.chillguys.nebulazone.infra.redis.vo;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.NoArgsConstructor;
import nbc.chillguys.nebulazone.domain.bid.entity.Bid;
import nbc.chillguys.nebulazone.domain.bid.entity.BidStatus;
import nbc.chillguys.nebulazone.domain.bid.exception.BidErrorCode;
import nbc.chillguys.nebulazone.domain.bid.exception.BidException;
import nbc.chillguys.nebulazone.domain.user.entity.User;

@Getter
@NoArgsConstructor
public class BidVo {
	private String bidUuid;
	private Long bidUserId;
	private String bidUserNickname;
	private String bidUserEmail;
	private Long bidPrice;
	private Long auctionId;
	private String bidStatus;
	private LocalDateTime bidCreatedAt;

	@JsonCreator
	public BidVo(
		@JsonProperty("bidUuid") String bidUuid,
		@JsonProperty("bidUserId") Long bidUserId,
		@JsonProperty("bidUserNickname") String bidUserNickname,
		@JsonProperty("bidUserEmail") String bidUserEmail,
		@JsonProperty("bidPrice") Long bidPrice,
		@JsonProperty("auctionId") Long auctionId,
		@JsonProperty("bidStatus") String bidStatus,
		@JsonProperty("bidCreatedAt") LocalDateTime bidCreatedAt
	) {
		this.bidUuid = bidUuid;
		this.bidUserId = bidUserId;
		this.bidUserNickname = bidUserNickname;
		this.bidUserEmail = bidUserEmail;
		this.bidPrice = bidPrice;
		this.auctionId = auctionId;
		this.bidStatus = bidStatus;
		this.bidCreatedAt = bidCreatedAt;
	}

	public static BidVo of(Long auctionId, User user, Long price) {
		return new BidVo(
			UUID.randomUUID().toString(),
			user.getId(),
			user.getNickname(),
			user.getEmail(),
			price,
			auctionId,
			BidStatus.BID.name(),
			LocalDateTime.now()
		);
	}

	public static BidVo fromBid(Bid bid) {
		return new BidVo(
			UUID.randomUUID().toString(),
			bid.getUser().getId(),
			bid.getUser().getNickname(),
			bid.getUser().getEmail(),
			bid.getPrice(),
			bid.getAuction().getId(),
			bid.getStatus().name(),
			bid.getCreatedAt()
		);
	}

	public void cancelBid() {
		this.bidStatus = BidStatus.CANCEL.name();
		this.bidCreatedAt = LocalDateTime.now();
	}

	public void validBidStatusIsWon() {
		if (bidStatus.equalsIgnoreCase(BidStatus.WON.name())) {
			throw new BidException(BidErrorCode.CANNOT_CANCEL_WON_BID);
		}
	}

	public void validBidStatusIsCancel() {
		if (bidStatus.equalsIgnoreCase(BidStatus.CANCEL.name())) {
			throw new BidException(BidErrorCode.ALREADY_BID_CANCELLED);
		}
	}

	public void validNotBidOwner(Long bidUserId) {
		if (!this.bidUserId.equals(bidUserId)) {
			throw new BidException(BidErrorCode.BID_NOT_OWNER);
		}
	}

	public void validMismatchBidOwner(Long bidUserId) {
		if (!this.bidUserId.equals(bidUserId)) {
			throw new BidException(BidErrorCode.BID_USER_MISMATCH);
		}
	}

	public void validAuctionMismatch(Long auctionId) {
		if (!Objects.equals(this.auctionId, auctionId)) {
			throw new BidException(BidErrorCode.BID_AUCTION_MISMATCH);

		}
	}

	public void wonBid() {
		this.bidStatus = BidStatus.WON.name();
	}

	public void updateStatus(String status) {
		this.bidStatus = status;
	}

}
