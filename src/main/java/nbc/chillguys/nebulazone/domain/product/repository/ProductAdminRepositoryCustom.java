package nbc.chillguys.nebulazone.domain.product.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import nbc.chillguys.nebulazone.domain.product.dto.ProductAdminSearchQueryCommand;
import nbc.chillguys.nebulazone.domain.product.entity.Product;

public interface ProductAdminRepositoryCustom {
	Page<Product> searchProducts(ProductAdminSearchQueryCommand command, Pageable pageable);

	Optional<Product> findByIdWithJoin(Long productId);

}
