package nbc.chillguys.nebulazone.domain.products.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import nbc.chillguys.nebulazone.domain.products.dto.ProductAdminSearchQueryCommand;
import nbc.chillguys.nebulazone.domain.products.entity.Product;

public interface ProductAdminRepositoryCustom {
	Page<Product> searchProducts(ProductAdminSearchQueryCommand command, Pageable pageable);

	Optional<Product> findByIdWithJoin(Long productId);

}
