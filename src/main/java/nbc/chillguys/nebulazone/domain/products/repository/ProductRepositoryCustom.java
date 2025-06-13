package nbc.chillguys.nebulazone.domain.products.repository;

import java.util.Optional;

import nbc.chillguys.nebulazone.domain.products.entity.Product;

public interface ProductRepositoryCustom {
	Optional<Product> findActiveProductById(Long productId);

	Optional<Product> findActiveProductByIdWithUserAndImages(Long productId);
}
