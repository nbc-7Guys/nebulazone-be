package nbc.chillguys.nebulazone.domain.comment.repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.domain.comment.dto.CommentWithUserInfo;
import nbc.chillguys.nebulazone.domain.comment.entity.QComment;
import nbc.chillguys.nebulazone.domain.user.entity.QUser;

@RequiredArgsConstructor
@Repository
public class CommentCustomRepositoryImpl implements CommentCustomRepository {

	private final JPAQueryFactory jpaQueryFactory;

	@Override
	public Page<CommentWithUserInfo> findComments(Long postId, int page, int size) {
		QComment comment = QComment.comment;
		QUser user = QUser.user;

		List<Long> parentIds = jpaQueryFactory
			.select(comment.id)
			.from(comment)
			.where(
				comment.post.id.eq(postId),
				comment.parent.isNull()
			)
			.orderBy(comment.id.desc())
			.offset((long) (page - 1) * size)
			.limit(size)
			.fetch();

		PageRequest pageable = PageRequest.of(page - 1, size);
		if (parentIds.isEmpty()) {
			return new PageImpl<>(Collections.emptyList(), pageable, 0L);
		}

		List<CommentWithUserInfo> flatList = jpaQueryFactory
			.select(Projections.constructor(CommentWithUserInfo.class,
				comment.id,
				comment.isDeleted.isTrue()
					.when(true).then("삭제된 댓글입니다.").otherwise(comment.content),
				user.nickname,
				comment.parent.id,
				comment.createdAt,
				comment.modifiedAt
			))
			.from(comment)
			.leftJoin(comment.user, user)
			.where(
				comment.post.id.eq(postId),
				comment.parent.id.in(parentIds).or(comment.id.in(parentIds))
			)
			.orderBy(comment.parent.id.asc().nullsFirst(), comment.id.asc())
			.fetch();

		Map<Long, CommentWithUserInfo> map = new LinkedHashMap<>();
		List<CommentWithUserInfo> result = new ArrayList<>();
		for (CommentWithUserInfo dto : flatList) {
			map.put(dto.commentId(), dto);
		}
		for (CommentWithUserInfo dto : flatList) {
			if (dto.parentId() == null) {
				result.add(dto);
			} else {
				CommentWithUserInfo parent = map.get(dto.parentId());
				if (parent != null) {
					parent.children().add(dto);
				}
			}
		}

		Long total = jpaQueryFactory
			.select(comment.count())
			.from(comment)
			.where(
				comment.post.id.eq(postId),
				comment.parent.isNull()
			)
			.fetchOne();

		return new PageImpl<>(result, pageable, total == null ? 0L : total);
	}
}
