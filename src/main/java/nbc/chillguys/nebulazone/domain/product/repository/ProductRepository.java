package nbc.chillguys.nebulazone.domain.product.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import nbc.chillguys.nebulazone.domain.product.entity.Product;

public interface ProductRepository
	extends JpaRepository<Product, Long>, ProductRepositoryCustom, ProductAdminRepositoryCustom {

}
