package nbc.chillguys.nebulazone.domain.product.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import nbc.chillguys.nebulazone.domain.product.vo.ProductDocument;

public interface ProductEsRepositoryCustom {
	Page<ProductDocument> searchProduct(String productName, String sellerNickname, String txMethod, Long priceFrom,
		Long priceTo,
		Pageable pageable);
}
