package nbc.chillguys.nebulazone.domain.auction.repository;

import static nbc.chillguys.nebulazone.domain.auction.entity.QAuction.*;
import static nbc.chillguys.nebulazone.domain.bid.entity.QBid.*;
import static nbc.chillguys.nebulazone.domain.product.entity.QProduct.*;
import static nbc.chillguys.nebulazone.domain.product.entity.QProductImage.*;
import static nbc.chillguys.nebulazone.domain.user.entity.QUser.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.domain.auction.dto.AuctionFindDetailInfo;
import nbc.chillguys.nebulazone.domain.auction.entity.Auction;

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
	public Optional<AuctionFindDetailInfo> findAuctionDetail(Long auctionId) {
		List<Tuple> tuple = jpaQueryFactory.select(
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
				productImage.url,
				product.createdAt
			)
			.from(auction)
			.join(auction.product, product)
			.join(product.seller, user)
			.leftJoin(auction.product.productImages, productImage)
			.where(auction.id.eq(auctionId),
				auction.deleted.eq(false),
				product.isDeleted.eq(false))
			.fetch();

		Long bidCount = jpaQueryFactory
			.select(bid.count())
			.from(bid)
			.where(bid.auction.id.eq(auctionId))
			.fetchOne();

		if (tuple.isEmpty()) {
			return Optional.empty();
		}

		List<String> imageUrlList = tuple.stream()
			.map(t -> t.get(productImage.url))
			.filter(Objects::nonNull)
			.toList();

		Tuple firstTuple = tuple.getFirst();

		AuctionFindDetailInfo info = new AuctionFindDetailInfo(
			firstTuple.get(auction.id),
			firstTuple.get(user.id),
			firstTuple.get(user.nickname),
			firstTuple.get(user.email),
			firstTuple.get(auction.startPrice),
			firstTuple.get(auction.currentPrice),
			Boolean.TRUE.equals(firstTuple.get(auction.isWon)),
			firstTuple.get(auction.endTime),
			firstTuple.get(product.id),
			firstTuple.get(product.name),
			imageUrlList,
			firstTuple.get(product.createdAt),
			bidCount != null ? bidCount : 0L
		);

		return Optional.of(info);
	}

	@Override
	public List<Auction> findActiveAuctionsForRecovery() {
		return jpaQueryFactory
			.selectFrom(auction)
			.join(auction.product, product).fetchJoin()
			.join(auction.product.seller, user).fetchJoin()
			.leftJoin(auction.product.productImages).fetchJoin()
			.where(
				auction.deleted.eq(false).or(auction.deleted.isNull()),
				auction.endTime.after(LocalDateTime.now()),
				auction.isWon.eq(false)
			)
			.fetch();
	}

	@Override
	public void updateCurrentPriceForBackup(Long auctionId, Long currentPrice) {
		jpaQueryFactory
			.update(auction)
			.set(auction.currentPrice, currentPrice)
			.where(auction.id.eq(auctionId))
			.execute();
	}

}
