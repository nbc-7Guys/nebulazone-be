package nbc.chillguys.nebulazone.domain.pointhistory.repository;

import static nbc.chillguys.nebulazone.domain.pointhistory.entity.QPointHistory.*;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.application.pointhistory.dto.request.PointHistoryAdminRequest;
import nbc.chillguys.nebulazone.application.pointhistory.dto.response.AdminPointHistoryResponse;
import nbc.chillguys.nebulazone.application.pointhistory.dto.response.QAdminPointHistoryResponse;
import nbc.chillguys.nebulazone.domain.user.entity.QUser;

@RequiredArgsConstructor
public class PointHistoryAdminRepositoryCustomImpl implements PointHistoryAdminRepositoryCustom {
	private final JPAQueryFactory queryFactory;

	@Override
	public Page<AdminPointHistoryResponse> searchAdminPointHistories(PointHistoryAdminRequest request,
		Pageable pageable) {
		QUser user = pointHistory.user;
		BooleanBuilder builder = new BooleanBuilder();

		// 조건 설정 (기존과 동일)
		if (request.email() != null && !request.email().isBlank()) {
			builder.and(user.email.containsIgnoreCase(request.email()));
		}
		if (request.nickname() != null && !request.nickname().isBlank()) {
			builder.and(user.nickname.containsIgnoreCase(request.nickname()));
		}
		if (request.type() != null) {
			builder.and(pointHistory.pointHistoryType.eq(request.type()));
		}
		if (request.status() != null) {
			builder.and(pointHistory.pointHistoryStatus.eq(request.status()));
		}
		if (request.startDate() != null) {
			builder.and(pointHistory.createdAt.goe(request.startDate()));
		}
		if (request.endDate() != null) {
			builder.and(pointHistory.createdAt.loe(request.endDate()));
		}

		// 데이터 조회 쿼리
		List<AdminPointHistoryResponse> results = queryFactory
			.select(new QAdminPointHistoryResponse(
				pointHistory.id,
				pointHistory.price,
				pointHistory.account,
				pointHistory.pointHistoryType,
				pointHistory.pointHistoryStatus,
				pointHistory.createdAt,
				user.id,
				user.email,
				user.nickname
			))
			.from(pointHistory)
			.join(pointHistory.user, user)
			.where(builder)
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize())
			.fetch();

		// 별도 COUNT 쿼리
		Long total = queryFactory
			.select(pointHistory.count())
			.from(pointHistory)
			.join(pointHistory.user, user)
			.where(builder)
			.fetchOne();

		return new PageImpl<>(results, pageable, total != null ? total : 0L);
	}

}
