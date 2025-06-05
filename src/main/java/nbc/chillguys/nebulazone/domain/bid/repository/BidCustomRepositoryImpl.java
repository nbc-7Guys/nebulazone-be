package nbc.chillguys.nebulazone.domain.bid.repository;

import static nbc.chillguys.nebulazone.domain.bid.entity.QBid.*;
import static nbc.chillguys.nebulazone.domain.products.entity.QProduct.*;
import static nbc.chillguys.nebulazone.domain.user.entity.QUser.*;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import jakarta.persistence.EntityManager;
import nbc.chillguys.nebulazone.domain.auction.entity.Auction;
import nbc.chillguys.nebulazone.domain.bid.dto.FindBidInfo;
import nbc.chillguys.nebulazone.domain.bid.dto.QFindBidInfo;

@Repository
public class BidCustomRepositoryImpl implements BidCustomRepository {

	private final JPAQueryFactory jpaQueryFactory;

	public BidCustomRepositoryImpl(EntityManager em) {
		this.jpaQueryFactory = new JPAQueryFactory(em);
	}

	@Override
	public Page<FindBidInfo> findBidsWithUserByAuction(Auction auction, int page, int size) {
		Pageable pageable = PageRequest.of(page, size);

		List<FindBidInfo> contents = jpaQueryFactory.select(
				new QFindBidInfo(
					bid.id,
					bid.price,
					bid.createdAt,
					bid.status,
					user.nickname,
					product.name
				))
			.from(bid)
			.join(bid.user, user)
			.join(bid.auction.product, product)
			.where(bid.auction.eq(auction))
			.orderBy(bid.createdAt.desc())
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize())
			.fetch();

		JPAQuery<Long> countQuery = jpaQueryFactory
			.select(bid.countDistinct())
			.from(bid)
			.where(bid.auction.eq(auction));

		return PageableExecutionUtils.getPage(contents, pageable, countQuery::fetchOne);
	}
}
