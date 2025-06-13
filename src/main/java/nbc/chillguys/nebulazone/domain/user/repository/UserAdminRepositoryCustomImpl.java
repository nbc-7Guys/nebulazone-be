package nbc.chillguys.nebulazone.domain.user.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.domain.user.dto.UserAdminSearchQueryCommand;
import nbc.chillguys.nebulazone.domain.user.entity.QUser;
import nbc.chillguys.nebulazone.domain.user.entity.User;

@Repository
@RequiredArgsConstructor
public class UserAdminRepositoryCustomImpl implements UserAdminRepositoryCustom {
	private final JPAQueryFactory jpaQueryFactory;

	@Override
	public Page<User> searchUsers(UserAdminSearchQueryCommand query, Pageable pageable) {
		QUser user = QUser.user;

		BooleanBuilder builder = new BooleanBuilder();
		if (query.keyword() != null && !query.keyword().isBlank()) {
			builder.and(
				user.email.containsIgnoreCase(query.keyword())
					.or(user.nickname.containsIgnoreCase(query.keyword()))
			);
		}
		if (query.userStatus() != null) {
			builder.and(user.status.eq(query.userStatus()));
		}
		if (query.roles() != null && !query.roles().isEmpty()) {
			builder.and(user.roles.any().in(query.roles()));
		}

		List<User> content = jpaQueryFactory
			.selectFrom(user)
			.leftJoin(user.roles).fetchJoin()
			.where(builder)
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize())
			.orderBy(user.createdAt.desc())
			.fetch();

		Long total = jpaQueryFactory
			.select(user.count())
			.from(user)
			.where(builder)
			.fetchOne();

		return new PageImpl<>(content, pageable, total != null ? total : 0L);
	}
}
