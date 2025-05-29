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
	private boolean isClosed;

	@Column(nullable = false)
	private LocalDateTime closedAt;

	private boolean isDeleted;
	private LocalDateTime deletedAt;

	@Builder
	public Auction(
		Long startPrice, Long currentPrice,
		LocalDateTime endTime,
		boolean isClosed, LocalDateTime closedAt,
		boolean isDeleted, LocalDateTime deletedAt
	) {
		this.startPrice = startPrice;
		this.currentPrice = currentPrice;
		this.endTime = endTime;
		this.isClosed = isClosed;
		this.closedAt = closedAt;
		this.isDeleted = isDeleted;
		this.deletedAt = deletedAt;
	}
}
