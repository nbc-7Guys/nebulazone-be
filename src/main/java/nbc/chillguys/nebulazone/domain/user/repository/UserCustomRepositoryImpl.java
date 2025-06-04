package nbc.chillguys.nebulazone.domain.user.repository;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.domain.user.entity.OAuthType;
import nbc.chillguys.nebulazone.domain.user.entity.QUser;
import nbc.chillguys.nebulazone.domain.user.entity.User;
import nbc.chillguys.nebulazone.domain.user.entity.UserStatus;

@Repository
@RequiredArgsConstructor
public class UserCustomRepositoryImpl implements UserCustomRepository {
	private final JPAQueryFactory jpaQueryFactory;

	@Override
	public Optional<User> findActiveUserByEmail(String email) {
		QUser user = QUser.user;

		return Optional.ofNullable(jpaQueryFactory.selectFrom(user)
			.leftJoin(user.roles).fetchJoin()
			.where(
				user.email.eq(email),
				user.status.eq(UserStatus.ACTIVE)
			).fetchOne());
	}

	@Override
	public Optional<User> findActiveUserById(Long userId) {
		QUser user = QUser.user;

		return Optional.ofNullable(jpaQueryFactory.selectFrom(user)
			.leftJoin(user.addresses).fetchJoin()
			.where(
				user.id.eq(userId),
				user.status.eq(UserStatus.ACTIVE)
			).fetchOne());
	}

	@Override
	public Optional<User> findActiveUserByEmailAndOAuthType(String email, OAuthType oAuthType) {
		QUser user = QUser.user;

		return Optional.ofNullable(jpaQueryFactory.selectFrom(user)
			.leftJoin(user.roles).fetchJoin()
			.where(
				user.email.eq(email),
				user.status.eq(UserStatus.ACTIVE),
				user.oAuthType.eq(oAuthType)
			).fetchOne());
	}
}
