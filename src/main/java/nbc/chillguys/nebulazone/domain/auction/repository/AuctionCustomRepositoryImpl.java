package nbc.chillguys.nebulazone.domain.auction.repository;

import static nbc.chillguys.nebulazone.domain.auction.entity.QAuction.*;
import static nbc.chillguys.nebulazone.domain.products.entity.QProduct.*;
import static nbc.chillguys.nebulazone.domain.products.entity.QProductImage.*;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import jakarta.persistence.EntityManager;
import nbc.chillguys.nebulazone.domain.auction.dto.AuctionFindInfo;
import nbc.chillguys.nebulazone.domain.auction.dto.QAuctionFindInfo;

@Repository
public class AuctionCustomRepositoryImpl implements AuctionCustomRepository {

	private final JPAQueryFactory jpaQueryFactory;

	public AuctionCustomRepositoryImpl(EntityManager em) {
		this.jpaQueryFactory = new JPAQueryFactory(em);
	}

	@Override
	public Page<AuctionFindInfo> findAllAuctionsWithProduct(int page, int size) {
		Pageable pageable = PageRequest.of(Math.max(page, 0), size);

		List<AuctionFindInfo> contents = jpaQueryFactory
			.select(new QAuctionFindInfo(
				auction.id,
				auction.startPrice,
				auction.currentPrice,
				auction.isClosed,
				auction.endTime,
				auction.createdAt,
				product.name,
				productImage.url.min()
			))
			.from(auction)
			.join(auction.product, product)
			.leftJoin(auction.product.productImages, productImage)
			.where(
				auction.isDeleted.eq(false),
				auction.deletedAt.isNull(),
				product.isDeleted.eq(false),
				product.deletedAt.isNull())
			.groupBy(auction.id, product.id)
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize())
			.orderBy(auction.createdAt.desc())
			.fetch();

		JPAQuery<Long> totalQuery = jpaQueryFactory
			.select(auction.countDistinct())
			.from(auction);

		return PageableExecutionUtils.getPage(contents, pageable, totalQuery::fetchOne);
	}
}
