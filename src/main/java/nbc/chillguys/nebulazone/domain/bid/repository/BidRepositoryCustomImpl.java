package nbc.chillguys.nebulazone.domain.bid.repository;

import static nbc.chillguys.nebulazone.domain.bid.entity.QBid.*;
import static nbc.chillguys.nebulazone.domain.product.entity.QProduct.*;
import static nbc.chillguys.nebulazone.domain.user.entity.QUser.*;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.domain.auction.entity.Auction;
import nbc.chillguys.nebulazone.domain.bid.dto.FindBidInfo;
import nbc.chillguys.nebulazone.domain.bid.dto.QFindBidInfo;
import nbc.chillguys.nebulazone.domain.bid.entity.Bid;
import nbc.chillguys.nebulazone.domain.bid.entity.BidStatus;
import nbc.chillguys.nebulazone.domain.user.entity.User;

@Repository
@RequiredArgsConstructor
public class BidRepositoryCustomImpl implements BidRepositoryCustom {

	private final JPAQueryFactory jpaQueryFactory;

	@Override
	public Page<FindBidInfo> findBidsWithUserByAuction(Auction auction, int page, int size) {
		Pageable pageable = PageRequest.of(page, size);

		List<FindBidInfo> contents = jpaQueryFactory.select(
				new QFindBidInfo(bid.id, bid.price, bid.createdAt, bid.status, user.nickname, product.name))
			.from(bid)
			.join(bid.user, user)
			.join(bid.auction.product, product)
			.where(bid.auction.eq(auction))
			.orderBy(bid.createdAt.desc())
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize())
			.fetch();

		JPAQuery<Long> countQuery = jpaQueryFactory.select(bid.countDistinct())
			.from(bid)
			.where(bid.auction.eq(auction));

		return PageableExecutionUtils.getPage(contents, pageable, countQuery::fetchOne);
	}

	@Override
	public Page<FindBidInfo> findMyBids(User loginUser, int page, int size) {
		Pageable pageable = PageRequest.of(page, size);

		List<FindBidInfo> contents = jpaQueryFactory
			.select(
				new QFindBidInfo(bid.id, bid.price, bid.createdAt, bid.status, user.nickname, product.name))
			.from(bid)
			.join(bid.user, user)
			.join(bid.auction.product, product)
			.where(bid.user.eq(loginUser))
			.orderBy(bid.createdAt.desc())
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize())
			.fetch();

		JPAQuery<Long> countQuery = jpaQueryFactory.select(bid.countDistinct()).from(bid).where(bid.user.eq(loginUser));

		return PageableExecutionUtils.getPage(contents, pageable, countQuery::fetchOne);
	}

	@Override
	public Bid findHighestPriceBidByAuctionWithUser(Long auctionId) {

		return jpaQueryFactory
			.selectFrom(bid)
			.join(bid.user, user)
			.fetchJoin()
			.where(bid.auction.id.eq(auctionId), bid.status.eq(BidStatus.BID))
			.orderBy(bid.price.desc())
			.limit(1)
			.fetchOne();
	}

	@Override
	public Optional<Bid> findBidWithWonUser(Long bidId) {

		return Optional.ofNullable(
			jpaQueryFactory
				.selectFrom(bid)
				.join(bid.user, user).fetchJoin()
				.where(bid.id.eq(bidId))
				.fetchOne());

	}

	@Override
	public List<Bid> findBidsByAuctionIdAndStatusBid(Long auctionId) {

		return jpaQueryFactory
			.select(bid)
			.from(bid)
			.join(bid.user, user).fetchJoin()
			.where(bid.auction.id.eq(auctionId), bid.status.eq(BidStatus.BID))
			.fetch();
	}
}
