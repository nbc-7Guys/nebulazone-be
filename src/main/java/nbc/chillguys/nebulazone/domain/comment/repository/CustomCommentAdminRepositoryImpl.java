package nbc.chillguys.nebulazone.domain.comment.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.domain.comment.dto.AdminCommentSearchQueryCommand;
import nbc.chillguys.nebulazone.domain.comment.entity.Comment;
import nbc.chillguys.nebulazone.domain.comment.entity.QComment;
import nbc.chillguys.nebulazone.domain.post.entity.QPost;
import nbc.chillguys.nebulazone.domain.user.entity.QUser;

@Repository
@RequiredArgsConstructor
public class CustomCommentAdminRepositoryImpl implements CustomCommentAdminRepository {
	private final JPAQueryFactory jpaQueryFactory;

	@Override
	public Page<Comment> searchComments(AdminCommentSearchQueryCommand command, Pageable pageable) {
		QComment comment = QComment.comment;
		QUser user = QUser.user;
		QPost post = QPost.post;

		BooleanBuilder builder = new BooleanBuilder();

		// 키워드(내용, 작성자명) 검색
		if (command.keyword() != null && !command.keyword().isBlank()) {
			builder.and(
				comment.content.containsIgnoreCase(command.keyword())
					.or(comment.user.nickname.containsIgnoreCase(command.keyword()))
			);
		}

		// 삭제여부 필터
		if (command.deleted() != null) {
			builder.and(comment.deleted.eq(command.deleted()));
		}

		List<Comment> content = jpaQueryFactory
			.selectFrom(comment)
			.leftJoin(comment.user, user).fetchJoin()
			.leftJoin(comment.post, post).fetchJoin()
			.leftJoin(comment.parent).fetchJoin()
			.where(builder)
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize())
			.orderBy(comment.createdAt.desc())
			.fetch();

		Long total = jpaQueryFactory
			.select(comment.count())
			.from(comment)
			.leftJoin(comment.user, user)
			.leftJoin(comment.post, post)
			.where(builder)
			.fetchOne();

		return new PageImpl<>(content, pageable, total != null ? total : 0L);
	}
}
