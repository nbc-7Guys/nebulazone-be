package nbc.chillguys.nebulazone.domain.review.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.domain.catalog.entity.QCatalog;
import nbc.chillguys.nebulazone.domain.review.entity.QReview;
import nbc.chillguys.nebulazone.domain.review.entity.Review;

@Repository
@RequiredArgsConstructor
public class ReviewCustomRepositoryImpl implements ReviewCustomRepository {

	private final JPAQueryFactory queryFactory;

	@Override
	public Page<Review> findReviews(Long catalogId, Pageable pageable) {
		QReview review = QReview.review;
		QCatalog catalog = QCatalog.catalog;

		List<Review> content = queryFactory
			.selectFrom(review)
			.leftJoin(review.catalog, catalog).fetchJoin()
			.where(review.catalog.id.eq(catalogId))
			.orderBy(review.createdAt.desc())
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize())
			.fetch();

		JPAQuery<Long> countQuery = queryFactory
			.select(review.count())
			.from(review)
			.where(review.catalog.id.eq(catalogId));

		return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
	}

}

