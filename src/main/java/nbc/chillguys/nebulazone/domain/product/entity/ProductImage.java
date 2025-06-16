package nbc.chillguys.nebulazone.domain.product.entity;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Embeddable
@NoArgsConstructor
public class ProductImage {

	private String url;

	public ProductImage(String url) {
		this.url = url;
	}

}
