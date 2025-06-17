package nbc.chillguys.nebulazone.domain.product.repository;

import static nbc.chillguys.nebulazone.domain.user.entity.QUser.*;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.domain.product.entity.Product;
import nbc.chillguys.nebulazone.domain.product.entity.QProduct;

@Repository
@RequiredArgsConstructor
public class ProductRepositoryCustomImpl implements ProductRepositoryCustom {
	private final JPAQueryFactory queryFactory;

	@Override
	public Optional<Product> findActiveProductById(Long productId) {
		QProduct product = QProduct.product;

		return Optional.ofNullable(
			queryFactory.selectFrom(product)
				.join(product.seller, user).fetchJoin()
				.where(
					product.id.eq(productId),
					product.isDeleted.isFalse()
				)
				.fetchOne()
		);
	}

	@Override
	public Optional<Product> findActiveProductByIdWithUserAndImages(Long productId) {
		QProduct product = QProduct.product;

		return Optional.ofNullable(
			queryFactory.selectFrom(product)
				.leftJoin(product.seller).fetchJoin()
				.leftJoin(product.productImages).fetchJoin()
				.where(
					product.id.eq(productId),
					product.isDeleted.isFalse()
				)
				.fetchOne()
		);
	}
}
