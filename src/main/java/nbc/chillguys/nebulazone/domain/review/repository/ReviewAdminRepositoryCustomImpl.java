package nbc.chillguys.nebulazone.domain.review.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.domain.catalog.entity.QCatalog;
import nbc.chillguys.nebulazone.domain.review.dto.ReviewAdminSearchQueryCommand;
import nbc.chillguys.nebulazone.domain.review.entity.QReview;
import nbc.chillguys.nebulazone.domain.review.entity.Review;

@Repository
@RequiredArgsConstructor
public class ReviewAdminRepositoryCustomImpl implements ReviewAdminRepositoryCustom {
	private final JPAQueryFactory jpaQueryFactory;

	@Override
	public Page<Review> searchReviews(ReviewAdminSearchQueryCommand command, Pageable pageable) {
		QReview review = QReview.review;
		QCatalog catalog = QCatalog.catalog;

		BooleanBuilder builder = new BooleanBuilder();

		// 키워드 검색 (리뷰 내용, 상품명)
		if (command.keyword() != null && !command.keyword().isBlank()) {
			builder.and(
				review.content.containsIgnoreCase(command.keyword())
					.or(review.catalog.name.containsIgnoreCase(command.keyword()))
			);
		}

		List<Review> content = jpaQueryFactory
			.selectFrom(review)
			.leftJoin(review.catalog, catalog).fetchJoin()
			.where(builder)
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize())
			.orderBy(review.createdAt.desc())
			.fetch();

		Long total = jpaQueryFactory
			.select(review.count())
			.from(review)
			.leftJoin(review.catalog, catalog)
			.where(builder)
			.fetchOne();

		return new PageImpl<>(content, pageable, total != null ? total : 0L);
	}
}
