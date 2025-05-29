package nbc.chillguys.nebulazone.domain.products.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import nbc.chillguys.nebulazone.domain.catalog.entity.Catalog;
import nbc.chillguys.nebulazone.domain.common.audit.BaseEntity;
import nbc.chillguys.nebulazone.domain.transaction.entity.TransactionMethod;
import nbc.chillguys.nebulazone.domain.user.entity.User;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "products")
public class Product extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "product_id")
	private Long id;

	@Column(nullable = false)
	private String name;

	@Lob
	@Column(nullable = false)
	private String description;

	@Column(nullable = false)
	private Long price;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private TransactionMethod method;

	private boolean isSold;

	private boolean isDeleted;

	private LocalDateTime deletedAt;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "seller_user_id")
	private User seller;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "catalog_id")
	private Catalog catalog;

	@Builder
	public Product(
		String name,
		String description,
		Long price,
		TransactionMethod method,
		boolean isSold,
		boolean isDeleted,
		LocalDateTime deletedAt,
		User seller,
		Catalog catalog
	) {
		this.name = name;
		this.description = description;
		this.price = price;
		this.method = method;
		this.isSold = isSold;
		this.isDeleted = isDeleted;
		this.deletedAt = deletedAt;
		this.seller = seller;
		this.catalog = catalog;
	}
}
