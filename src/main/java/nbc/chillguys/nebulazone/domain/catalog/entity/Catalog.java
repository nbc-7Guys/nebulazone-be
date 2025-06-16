package nbc.chillguys.nebulazone.domain.catalog.entity;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import nbc.chillguys.nebulazone.domain.common.audit.BaseEntity;
import nbc.chillguys.nebulazone.domain.review.entity.Review;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "catalogs")
public class Catalog extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "catalog_id")
	private Long id;

	@Column(nullable = false)
	private String name;

	@Column(name = "product_code", nullable = false)
	private Long productCode;

	@Column(columnDefinition = "LONGTEXT NOT NULL")
	private String description;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private CatalogType type;

	@OneToMany(mappedBy = "catalog")
	private List<Review> reviews = new ArrayList<>();

	@Builder
	public Catalog(String name, String description, CatalogType type) {
		this.name = name;
		this.description = description;
		this.type = type;
	}

	public void update(String name, String description, CatalogType type) {
		if (name != null) {
			this.name = name;
		}
		if (description != null) {
			this.description = description;
		}
		if (type != null) {
			this.type = type;
		}
	}

}
