package nbc.chillguys.nebulazone.domain.auction.repository;

import static nbc.chillguys.nebulazone.domain.auction.entity.QAuction.*;
import static nbc.chillguys.nebulazone.domain.bid.entity.QBid.*;
import static nbc.chillguys.nebulazone.domain.product.entity.QProduct.*;
import static nbc.chillguys.nebulazone.domain.product.entity.QProductImage.*;
import static nbc.chillguys.nebulazone.domain.user.entity.QUser.*;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.domain.auction.dto.AuctionFindDetailInfo;
import nbc.chillguys.nebulazone.domain.auction.dto.QAuctionFindDetailInfo;
import nbc.chillguys.nebulazone.domain.auction.entity.Auction;
import nbc.chillguys.nebulazone.domain.bid.entity.BidStatus;

@Repository
@RequiredArgsConstructor
public class AuctionRepositoryCustomImpl implements AuctionRepositoryCustom {

	private final JPAQueryFactory jpaQueryFactory;

	@Override
	public Optional<Auction> findAuctionWithProductAndSeller(Long auctionId) {
		return Optional.ofNullable(jpaQueryFactory
			.selectFrom(auction)
			.join(auction.product, product).fetchJoin()
			.join(product.seller, user).fetchJoin()
			.where(auction.id.eq(auctionId),
				auction.deleted.eq(false),
				product.isDeleted.eq(false))
			.fetchOne());
	}

	@Override
	public Optional<Auction> findByAuctionWithProduct(Long auctionId) {

		return Optional.ofNullable(
			jpaQueryFactory
				.selectFrom(auction)
				.join(auction.product, product).fetchJoin()
				.where(auction.id.eq(auctionId))
				.fetchOne()

		);
	}

	@Override
	public List<Auction> findAuctionsByNotDeletedAndIsWonFalse() {

		return jpaQueryFactory
			.selectFrom(auction)
			.join(auction.product, product).fetchJoin()
			.join(product.seller, user).fetchJoin()
			.where(auction.deleted.eq(false),
				auction.isWon.eq(false))
			.fetch();
	}

	@Override
	public Optional<AuctionFindDetailInfo> findAuctionDetail(Long auctionId) {
		return Optional.ofNullable(jpaQueryFactory.select(
				new QAuctionFindDetailInfo(
					auction.id,
					user.id,
					user.nickname,
					user.email,
					auction.startPrice,
					auction.currentPrice,
					auction.isWon,
					auction.endTime,
					product.id,
					product.name,
					productImage.url.min(),
					product.createdAt,
					bid.auction.id.count()
				))
			.from(auction)
			.join(auction.product, product)
			.join(product.seller, user)
			.leftJoin(auction.product.productImages, productImage)
			.leftJoin(bid)
			.on(bid.auction.eq(auction),
				bid.status.notIn(BidStatus.CANCEL))
			.where(auction.id.eq(auctionId),
				auction.deleted.eq(false),
				product.isDeleted.eq(false))
			.fetchOne());
	}

}
