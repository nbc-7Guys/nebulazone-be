package nbc.chillguys.nebulazone.domain.product.repository;

import java.util.Optional;

import nbc.chillguys.nebulazone.domain.product.entity.Product;

public interface ProductRepositoryCustom {
	Optional<Product> findActiveProductById(Long productId);

	Optional<Product> findActiveProductByIdWithUserAndImages(Long productId);
}
