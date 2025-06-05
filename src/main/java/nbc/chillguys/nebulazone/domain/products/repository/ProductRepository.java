package nbc.chillguys.nebulazone.domain.products.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import nbc.chillguys.nebulazone.domain.products.entity.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {

	Optional<Product> findByIdAndDeletedFalse(Long id);
}
