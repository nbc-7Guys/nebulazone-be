package nbc.chillguys.nebulazone.domain.auction.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
import nbc.chillguys.nebulazone.domain.common.audit.BaseEntity;
import nbc.chillguys.nebulazone.domain.products.entity.Product;
import nbc.chillguys.nebulazone.domain.user.entity.User;

@Getter
@Entity
@Table(name = "auctions")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Auction extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "auctions_id")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "product_id", nullable = false)
	private Product product;

	@Column(nullable = false)
	private Long startPrice;

	private Long currentPrice;

	@Column(nullable = false)
	private LocalDateTime endTime;

	@Column(nullable = false)
	private boolean isWon;

	@Column(name = "is_deleted")
	private boolean deleted;
	private LocalDateTime deletedAt;

	@Builder
	private Auction(
		Product product, Long startPrice, Long currentPrice,
		LocalDateTime endTime, boolean isWon,
		boolean isDeleted, LocalDateTime deletedAt
	) {
		this.product = product;
		this.startPrice = startPrice;
		this.currentPrice = currentPrice;
		this.endTime = endTime;
		this.isWon = isWon;
		this.deleted = isDeleted;
		this.deletedAt = deletedAt;
	}

	public Long delete() {
		this.deleted = true;
		this.deletedAt = LocalDateTime.now();
		return id;
	}

	public void update(Long startPrice, Long currentPrice, LocalDateTime endTime, Boolean isWon) {
		this.startPrice = startPrice;
		this.currentPrice = currentPrice;
		this.endTime = endTime;
		if (isWon != null) {
			this.isWon = isWon;
		}
	}

	public void restore() {
		this.deleted = false;
		this.deletedAt = null;
	}

	public void wonAuction() {
		isWon = true;
	}

	public void updateBidPrice(Long price) {
		this.currentPrice = price;
	}

	public boolean isAuctionOwner(User user) {
		return product.getSeller().getId().equals(user.getId());
	}

	public boolean isDeleted() {
		return deleted && deletedAt != null;
	}

	public void updateEndTime() {
		this.endTime = LocalDateTime.now();
	}
}
