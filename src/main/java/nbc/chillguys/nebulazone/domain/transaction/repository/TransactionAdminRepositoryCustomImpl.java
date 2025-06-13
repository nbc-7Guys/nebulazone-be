package nbc.chillguys.nebulazone.domain.transaction.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.domain.products.entity.QProduct;
import nbc.chillguys.nebulazone.domain.transaction.dto.TransactionAdminSearchQueryCommand;
import nbc.chillguys.nebulazone.domain.transaction.entity.QTransaction;
import nbc.chillguys.nebulazone.domain.transaction.entity.Transaction;
import nbc.chillguys.nebulazone.domain.user.entity.QUser;

@Repository
@RequiredArgsConstructor
public class TransactionAdminRepositoryCustomImpl implements TransactionAdminRepositoryCustom {
	private final JPAQueryFactory jpaQueryFactory;

	@Override
	public Page<Transaction> searchTransactions(TransactionAdminSearchQueryCommand command, Pageable pageable) {
		QTransaction tx = QTransaction.transaction;
		QUser user = QUser.user;
		QProduct product = QProduct.product;

		BooleanBuilder builder = new BooleanBuilder();
		if (command.keyword() != null && !command.keyword().isBlank()) {
			builder.and(
				tx.product.name.containsIgnoreCase(command.keyword())
					.or(tx.user.nickname.containsIgnoreCase(command.keyword()))
			);
		}
		if (command.method() != null) {
			builder.and(tx.method.eq(command.method()));
		}
		List<Transaction> content = jpaQueryFactory
			.selectFrom(tx)
			.leftJoin(tx.user, user).fetchJoin()
			.leftJoin(tx.product, product).fetchJoin()
			.where(builder)
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize())
			.orderBy(tx.createdAt.desc())
			.fetch();
		Long total = jpaQueryFactory
			.select(tx.count())
			.from(tx)
			.leftJoin(tx.user, user)
			.leftJoin(tx.product, product)
			.where(builder)
			.fetchOne();
		return new PageImpl<>(content, pageable, total != null ? total : 0L);
	}

}
