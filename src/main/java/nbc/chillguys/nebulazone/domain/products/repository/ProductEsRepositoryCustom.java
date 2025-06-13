package nbc.chillguys.nebulazone.domain.products.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import nbc.chillguys.nebulazone.domain.products.vo.ProductDocument;

public interface ProductEsRepositoryCustom {
	Page<ProductDocument> searchProduct(String productName, String txMethod, Long priceFrom, Long priceTo,
		Pageable pageable);
}
