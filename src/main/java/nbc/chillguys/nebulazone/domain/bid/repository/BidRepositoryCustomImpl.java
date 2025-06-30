package nbc.chillguys.nebulazone.domain.bid.repository;

import static nbc.chillguys.nebulazone.domain.auction.entity.QAuction.*;
import static nbc.chillguys.nebulazone.domain.bid.entity.QBid.*;
import static nbc.chillguys.nebulazone.domain.product.entity.QProduct.*;
import static nbc.chillguys.nebulazone.domain.user.entity.QUser.*;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.domain.bid.dto.FindBidsByAuctionInfo;
import nbc.chillguys.nebulazone.domain.bid.dto.FindMyBidsInfo;
import nbc.chillguys.nebulazone.domain.bid.dto.QFindBidsByAuctionInfo;
import nbc.chillguys.nebulazone.domain.bid.dto.QFindMyBidsInfo;
import nbc.chillguys.nebulazone.domain.bid.entity.Bid;
import nbc.chillguys.nebulazone.domain.bid.entity.BidStatus;

@Repository
@RequiredArgsConstructor
public class BidRepositoryCustomImpl implements BidRepositoryCustom {

	private final JPAQueryFactory jpaQueryFactory;

	@Override
	public Page<FindBidsByAuctionInfo> findBidsWithUserByAuctionId(Long auctionId, int page, int size) {

		Pageable pageable = PageRequest.of(page, size);
		List<FindBidsByAuctionInfo> contents = jpaQueryFactory.select(
				new QFindBidsByAuctionInfo(
					bid.price,
					bid.createdAt,
					bid.status,
					user.nickname,
					auction.id))
			.from(bid)
			.join(bid.user, user)
			.join(bid.auction, auction)
			.where(bid.auction.id.eq(auctionId))
			.orderBy(
				bid.createdAt.desc(),
				bid.price.desc())
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize())
			.fetch();

		JPAQuery<Long> countQuery = jpaQueryFactory.select(bid.countDistinct())
			.from(bid)
			.where(bid.auction.id.eq(auctionId));

		return PageableExecutionUtils.getPage(contents, pageable, countQuery::fetchOne);
	}

	@Override
	public List<FindMyBidsInfo> findMyBids(Long userId) {

		return jpaQueryFactory
			.select(
				new QFindMyBidsInfo(
					user.id,
					user.nickname,
					bid.status,
					bid.price,
					bid.createdAt,
					auction.id,
					product.id,
					product.name)
			)
			.from(bid)
			.join(bid.user, user)
			.join(bid.auction, auction)
			.join(bid.auction.product, product)
			.where(bid.user.id.eq(userId))
			.orderBy(bid.createdAt.desc())
			.fetch();

	}

	@Override
	public Bid findHighestPriceBidByAuctionWithUser(Long auctionId) {

		return jpaQueryFactory
			.selectFrom(bid)
			.join(bid.user, user)
			.fetchJoin()
			.where(
				bid.auction.id.eq(auctionId),
				bid.status.eq(BidStatus.BID))
			.orderBy(bid.price.desc())
			.limit(1)
			.fetchOne();
	}

	@Override
	public List<Bid> findBidsByAuctionIdAndStatusBid(Long auctionId) {

		return jpaQueryFactory
			.select(bid)
			.from(bid)
			.join(bid.user, user).fetchJoin()
			.where(
				bid.auction.id.eq(auctionId),
				bid.status.eq(BidStatus.BID))
			.fetch();
	}
}
