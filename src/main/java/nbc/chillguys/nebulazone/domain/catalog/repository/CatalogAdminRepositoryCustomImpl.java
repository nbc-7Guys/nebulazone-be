package nbc.chillguys.nebulazone.domain.catalog.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.domain.catalog.dto.CatalogAdminSearchQueryCommand;
import nbc.chillguys.nebulazone.domain.catalog.entity.Catalog;
import nbc.chillguys.nebulazone.domain.catalog.entity.QCatalog;

@Repository
@RequiredArgsConstructor
public class CatalogAdminRepositoryCustomImpl implements CatalogAdminRepositoryCustom {
	private final JPAQueryFactory jpaQueryFactory;

	@Override
	public Page<Catalog> searchCatalogs(CatalogAdminSearchQueryCommand command, Pageable pageable) {
		QCatalog catalog = QCatalog.catalog;
		BooleanBuilder builder = new BooleanBuilder();

		if (command.keyword() != null && !command.keyword().isBlank()) {
			builder.and(
				catalog.name.containsIgnoreCase(command.keyword())
					.or(catalog.description.containsIgnoreCase(command.keyword()))
			);
		}
		if (command.type() != null) {
			builder.and(catalog.type.eq(command.type()));
		}

		List<Catalog> content = jpaQueryFactory
			.selectFrom(catalog)
			.where(builder)
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize())
			.orderBy(catalog.createdAt.desc())
			.fetch();

		Long total = jpaQueryFactory
			.select(catalog.count())
			.from(catalog)
			.where(builder)
			.fetchOne();

		return new PageImpl<>(content, pageable, total != null ? total : 0L);
	}
}
