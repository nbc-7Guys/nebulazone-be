package nbc.chillguys.nebulazone.domain.transaction.repository;

import static nbc.chillguys.nebulazone.domain.product.entity.QProduct.*;
import static nbc.chillguys.nebulazone.domain.transaction.entity.QTransaction.*;
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
import nbc.chillguys.nebulazone.domain.transaction.dto.QTransactionFindAllInfo;
import nbc.chillguys.nebulazone.domain.transaction.dto.QTransactionFindDetailInfo;
import nbc.chillguys.nebulazone.domain.transaction.dto.TransactionFindAllInfo;
import nbc.chillguys.nebulazone.domain.transaction.dto.TransactionFindDetailInfo;
import nbc.chillguys.nebulazone.domain.user.entity.User;

@Repository
@RequiredArgsConstructor
public class TransactionRepositoryCustomImpl implements TransactionRepositoryCustom {

	private final JPAQueryFactory jpaQueryFactory;

	@Override
	public Page<TransactionFindAllInfo> findTransactionsWithProductAndUser(User loginUser, int page, int size) {
		Pageable pageable = PageRequest.of(page, size);

		List<TransactionFindAllInfo> contents = jpaQueryFactory
			.select(new QTransactionFindAllInfo(
				transaction.id,
				transaction.price,
				transaction.method,
				transaction.createdAt,
				product.name,
				product.isSold
			))
			.from(transaction)
			.join(transaction.user, user)
			.join(transaction.product, product)
			.where(transaction.user.id.eq(loginUser.getId()))
			.orderBy(transaction.createdAt.desc())
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize())
			.fetch();

		JPAQuery<Long> countQuery = jpaQueryFactory
			.select(transaction.countDistinct())
			.from(transaction)
			.where(transaction.user.id.eq(loginUser.getId()));

		return PageableExecutionUtils.getPage(contents, pageable, countQuery::fetchOne);
	}

	@Override
	public Optional<TransactionFindDetailInfo> findTransactionWithProductAndUser(User loginUser, Long transactionId) {

		return Optional.ofNullable(jpaQueryFactory
			.select(new QTransactionFindDetailInfo(
				transaction.id,
				user.id,
				user.nickname,
				user.email,
				transaction.price,
				transaction.createdAt,
				transaction.method,
				product.id,
				product.name,
				product.createdAt,
				product.isSold
			))
			.from(transaction)
			.join(transaction.user, user)
			.join(transaction.product, product)
			.where(user.id.eq(loginUser.getId()),
				transaction.id.eq(transactionId))
			.fetchOne());
	}
}
