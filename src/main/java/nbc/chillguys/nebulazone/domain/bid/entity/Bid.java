package nbc.chillguys.nebulazone.domain.bid.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import nbc.chillguys.nebulazone.domain.auction.entity.Auction;
import nbc.chillguys.nebulazone.domain.common.audit.BaseEntity;
import nbc.chillguys.nebulazone.domain.user.entity.User;

@Getter
@Entity
@Table(name = "bids")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Bid extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "bid_id")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "auction_id")
	private Auction auction;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Column(nullable = false)
	private Long price;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private BidStatus status;

	@Builder
	private Bid(Auction auction, User user, Long price) {
		this.auction = auction;
		this.user = user;
		this.price = price;
		this.status = BidStatus.BID;
	}

	public void updateStatus(BidStatus status) {
		this.status = status;
	}

	public void cancelBid() {
		this.status = BidStatus.CANCEL;
	}

	public void wonBid() {
		this.status = BidStatus.WON;
	}

	public boolean isNotBidOwner(User user) {
		return !user.equals(this.user);
	}

	public boolean isDifferentAuction(Auction auction) {
		return !this.auction.equals(auction);
	}

	public void updateBidPrice(Long price) {
		this.price = price;
	}

}
