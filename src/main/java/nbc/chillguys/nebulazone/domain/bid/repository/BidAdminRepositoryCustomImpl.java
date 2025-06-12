package nbc.chillguys.nebulazone.domain.bid.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.domain.auction.entity.QAuction;
import nbc.chillguys.nebulazone.domain.bid.dto.AdminBidSearchQueryCommand;
import nbc.chillguys.nebulazone.domain.bid.entity.Bid;
import nbc.chillguys.nebulazone.domain.bid.entity.QBid;
import nbc.chillguys.nebulazone.domain.user.entity.QUser;

@Repository
@RequiredArgsConstructor
public class BidAdminRepositoryCustomImpl implements BidAdminRepositoryCustom {
	private final JPAQueryFactory jpaQueryFactory;

	@Override
	public Page<Bid> searchBids(AdminBidSearchQueryCommand command, Pageable pageable) {
		QBid bid = QBid.bid;
		QAuction auction = QAuction.auction;
		QUser user = QUser.user;

		BooleanBuilder builder = new BooleanBuilder();

		if (command.auctionId() != null) {
			builder.and(bid.auction.id.eq(command.auctionId()));
		}
		if (command.userId() != null) {
			builder.and(bid.user.id.eq(command.userId()));
		}
		if (command.status() != null) {
			builder.and(bid.status.eq(command.status()));
		}

		List<Bid> content = jpaQueryFactory
			.selectFrom(bid)
			.leftJoin(bid.auction, auction).fetchJoin()
			.leftJoin(bid.user, user).fetchJoin()
			.where(builder)
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize())
			.orderBy(bid.createdAt.desc())
			.fetch();

		Long total = jpaQueryFactory
			.select(bid.count())
			.from(bid)
			.where(builder)
			.fetchOne();

		return new PageImpl<>(content, pageable, total != null ? total : 0L);
	}
}
