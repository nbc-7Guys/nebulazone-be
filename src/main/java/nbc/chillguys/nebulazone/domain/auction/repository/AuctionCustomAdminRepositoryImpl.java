package nbc.chillguys.nebulazone.domain.auction.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.domain.auction.dto.AuctionAdminSearchQueryCommand;
import nbc.chillguys.nebulazone.domain.auction.entity.Auction;
import nbc.chillguys.nebulazone.domain.auction.entity.QAuction;
import nbc.chillguys.nebulazone.domain.products.entity.QProduct;

@Repository
@RequiredArgsConstructor
public class AuctionCustomAdminRepositoryImpl implements AuctionCustomAdminRepository {
	private final JPAQueryFactory jpaQueryFactory;

	@Override
	public Page<Auction> searchAuctions(AuctionAdminSearchQueryCommand command, Pageable pageable) {
		QAuction auction = QAuction.auction;
		QProduct product = QProduct.product;

		BooleanBuilder builder = new BooleanBuilder();

		// 키워드(상품명) 검색
		if (command.keyword() != null && !command.keyword().isBlank()) {
			builder.and(auction.product.name.containsIgnoreCase(command.keyword()));
		}
		// 삭제여부
		if (command.deleted() != null) {
			builder.and(auction.deleted.eq(command.deleted()));
		}
		// 낙찰여부
		if (command.isWon() != null) {
			builder.and(auction.isWon.eq(command.isWon()));
		}

		List<Auction> content = jpaQueryFactory
			.selectFrom(auction)
			.leftJoin(auction.product, product).fetchJoin()
			.where(builder)
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize())
			.orderBy(auction.createdAt.desc())
			.fetch();

		Long total = jpaQueryFactory
			.select(auction.count())
			.from(auction)
			.where(builder)
			.fetchOne();

		return new PageImpl<>(content, pageable, total != null ? total : 0L);
	}
}
