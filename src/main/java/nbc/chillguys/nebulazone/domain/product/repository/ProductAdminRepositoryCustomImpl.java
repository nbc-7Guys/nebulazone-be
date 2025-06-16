package nbc.chillguys.nebulazone.domain.product.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.domain.product.dto.ProductAdminSearchQueryCommand;
import nbc.chillguys.nebulazone.domain.product.entity.Product;
import nbc.chillguys.nebulazone.domain.product.entity.QProduct;

@Repository
@RequiredArgsConstructor
public class ProductAdminRepositoryCustomImpl implements ProductAdminRepositoryCustom {
	private final JPAQueryFactory jpaQueryFactory;

	@Override
	public Page<Product> searchProducts(ProductAdminSearchQueryCommand command, Pageable pageable) {
		QProduct product = QProduct.product;

		BooleanBuilder builder = new BooleanBuilder();

		// 키워드 검색 (이름/설명)
		if (command.keyword() != null && !command.keyword().isBlank()) {
			builder.and(product.name.containsIgnoreCase(command.keyword())
				.or(product.description.containsIgnoreCase(command.keyword())));
		}

		// 거래방식(ProductTxMethod) 필터
		if (command.txMethod() != null) {
			builder.and(product.txMethod.eq(command.txMethod()));
		}

		// 판매여부
		if (command.isSold() != null) {
			builder.and(product.isSold.eq(command.isSold()));
		}

		// 소프트 딜리트 제외(삭제글 미포함)
		builder.and(product.isDeleted.eq(false));

		// 실제 데이터 조회
		List<Product> content = jpaQueryFactory
			.selectFrom(product)
			.leftJoin(product.seller).fetchJoin()
			.leftJoin(product.catalog).fetchJoin()
			.leftJoin(product.productImages).fetchJoin()
			.where(builder)
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize())
			.orderBy(product.createdAt.desc())
			.fetch();

		// 전체 카운트
		Long total = jpaQueryFactory
			.select(product.count())
			.from(product)
			.where(builder)
			.fetchOne();

		return new PageImpl<>(content, pageable, total != null ? total : 0L);
	}

	@Override
	public Optional<Product> findByIdWithJoin(Long productId) {
		QProduct product = QProduct.product;
		Product result = jpaQueryFactory
			.selectFrom(product)
			.leftJoin(product.seller).fetchJoin()
			.leftJoin(product.catalog).fetchJoin()
			.leftJoin(product.productImages).fetchJoin()
			.where(product.id.eq(productId))
			.fetchOne();
		return Optional.ofNullable(result);
	}

}
