package nbc.chillguys.nebulazone.domain.auction.repository;

import static nbc.chillguys.nebulazone.domain.auction.entity.QAuction.*;
import static nbc.chillguys.nebulazone.domain.bid.entity.QBid.*;
import static nbc.chillguys.nebulazone.domain.products.entity.QProduct.*;
import static nbc.chillguys.nebulazone.domain.products.entity.QProductImage.*;
import static nbc.chillguys.nebulazone.domain.user.entity.QUser.*;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import nbc.chillguys.nebulazone.domain.auction.dto.AuctionFindInfo;
import nbc.chillguys.nebulazone.domain.auction.dto.QAuctionFindInfo;
import nbc.chillguys.nebulazone.domain.auction.entity.Auction;
import nbc.chillguys.nebulazone.domain.auction.entity.AuctionSortType;

@Repository
public class AuctionCustomRepositoryImpl implements AuctionCustomRepository {

	private final JPAQueryFactory jpaQueryFactory;

	public AuctionCustomRepositoryImpl(EntityManager em) {
		this.jpaQueryFactory = new JPAQueryFactory(em);
	}

	@Override
	public Page<AuctionFindInfo> findAuctionsWithProduct(int page, int size) {
		Pageable pageable = PageRequest.of(page, size);

		List<AuctionFindInfo> contents = jpaQueryFactory
			.select(new QAuctionFindInfo(
				auction.id,
				auction.startPrice,
				auction.currentPrice,
				auction.isWon,
				auction.endTime,
				auction.createdAt,
				product.name,
				productImage.url.min(),
				bid.auction.id.count()
			))
			.from(auction)
			.join(auction.product, product)
			.leftJoin(auction.product.productImages, productImage)
			.leftJoin(bid).on(bid.auction.eq(auction))
			.where(
				auction.deleted.eq(false),
				auction.deletedAt.isNull(),
				product.isDeleted.eq(false),
				product.deletedAt.isNull())
			.groupBy(auction.id, product.id)
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize())
			.orderBy(auction.createdAt.desc())
			.fetch();

		JPAQuery<Long> countQuery = jpaQueryFactory
			.select(auction.countDistinct())
			.from(auction)
			.where(
				auction.deleted.eq(false),
				auction.deletedAt.isNull(),
				product.isDeleted.eq(false),
				product.deletedAt.isNull()
			);

		return PageableExecutionUtils.getPage(contents, pageable, countQuery::fetchOne);
	}

	@Override
	public List<AuctionFindInfo> finAuctionsBySortType(AuctionSortType sortType) {

		OrderSpecifier<?> orderType = switch (sortType) {
			case POPULAR -> bid.id.count().desc();
			case CLOSING -> auction.endTime.desc();
		};

		return jpaQueryFactory
			.select(new QAuctionFindInfo(
				auction.id,
				auction.startPrice,
				auction.currentPrice,
				auction.isWon,
				auction.endTime,
				auction.createdAt,
				product.name,
				productImage.url.min(),
				bid.auction.id.count()
			))
			.from(auction)
			.join(auction.product, product)
			.leftJoin(auction.product.productImages, productImage)
			.leftJoin(bid).on(bid.auction.eq(auction))
			.where(
				auction.isWon.eq(false),
				auction.deleted.eq(false),
				auction.deletedAt.isNull(),
				product.isDeleted.eq(false),
				product.deletedAt.isNull())
			.groupBy(auction.id, product.id)
			.limit(5)
			.orderBy(orderType)
			.fetch();
	}

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("""
		select a from Auction a
		join fetch a.product p
		join fetch p.seller s
		where a.id = :auctionId and a.deleted = false
		""")

	@Override
	public Optional<Auction> findAuctionWithProductAndSellerLock(Long id) {

		return Optional.ofNullable(jpaQueryFactory
			.selectFrom(auction)
			.join(auction.product, product).fetchJoin()
			.join(product.seller, user).fetchJoin()
			.where(auction.deleted.eq(false)
				.and(product.deleted.eq(false)))
			.setLockMode(LockModeType.PESSIMISTIC_WRITE)
			.fetchOne());
	}

	@Override
	public List<Auction> findAuctionsByNotDeletedAndIsWonFalse() {

		return jpaQueryFactory
			.selectFrom(auction)
			.where(auction.deleted.eq(false)
				.and(auction.isWon.eq(false)))
			.fetch();
	}

}
