package nbc.chillguys.nebulazone.domain.products.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
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
	private ProductTxMethod txMethod;

	private boolean isSold;

	private boolean isDeleted;

	private LocalDateTime deletedAt;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "seller_user_id", nullable = false)
	private User seller;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "catalog_id")    // todo: 카탈로그 생성코드가 완성되면 추후 nullable = false 작성
	private Catalog catalog;

	@ElementCollection
	@CollectionTable(name = "product_images", joinColumns = @JoinColumn(name = "product_id"))
	@Column(name = "image_url")
	private List<ProductImage> productImages = new ArrayList<>();

	@Builder
	public Product(
		String name,
		String description,
		Long price,
		ProductTxMethod txMethod,
		boolean isSold,
		boolean isDeleted,
		LocalDateTime deletedAt,
		User seller,
		Catalog catalog
	) {
		this.name = name;
		this.description = description;
		this.price = price;
		this.txMethod = txMethod;
		this.isSold = isSold;
		this.isDeleted = isDeleted;
		this.deletedAt = deletedAt;
		this.seller = seller;
		this.catalog = catalog;
	}

	public static Product of(String name, String description, Long price, ProductTxMethod txMethod,
		User seller, Catalog catalog) {
		return Product.builder()
			.name(name)
			.description(description)
			.price(price)
			.txMethod(txMethod)
			.seller(seller)
			.catalog(catalog)
			.build();
	}

	public void addProductImages(List<String> productImageUrls) {
		if (productImageUrls != null) {
			this.productImages.addAll(productImageUrls.stream()
				.map(ProductImage::new)
				.toList());
		}

	}

	public void update(String name, String description) {
		this.name = name;
		this.description = description;
	}
}
