package nbc.chillguys.nebulazone.domain.review.entity;

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
import nbc.chillguys.nebulazone.domain.catalog.entity.Catalog;
import nbc.chillguys.nebulazone.domain.common.audit.BaseEntity;

@Getter
@Entity
@Table(name = "reviews")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Review extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "review_id")
	private Long id;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String content;

	@Column(nullable = false)
	private int star;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "catalog_id", nullable = false)
	private Catalog catalog;

	@Builder
	public Review(String content, int star, Catalog catalog) {
		this.content = content;
		this.star = star;
		this.catalog = catalog;
	}

	public void update(String content, Integer star) {
		if (content != null) {
			this.content = content;
		}
		if (star != null) {
			this.star = star;
		}
	}

}
