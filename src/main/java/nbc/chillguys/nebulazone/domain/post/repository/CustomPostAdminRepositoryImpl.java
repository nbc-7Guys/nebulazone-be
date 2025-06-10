package nbc.chillguys.nebulazone.domain.post.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.domain.post.dto.AdminPostSearchQueryCommand;
import nbc.chillguys.nebulazone.domain.post.entity.Post;
import nbc.chillguys.nebulazone.domain.post.entity.QPost;

@Repository
@RequiredArgsConstructor
public class CustomPostAdminRepositoryImpl implements CustomPostAdminRepository {
	private final JPAQueryFactory jpaQueryFactory;

	@Override
	public Page<Post> searchPosts(AdminPostSearchQueryCommand command, Pageable pageable) {
		QPost post = QPost.post;

		BooleanBuilder builder = new BooleanBuilder();
		if (command.keyword() != null && !command.keyword().isBlank()) {
			builder.and(
				post.title.containsIgnoreCase(command.keyword())
					.or(post.content.containsIgnoreCase(command.keyword()))
			);
		}
		if (command.type() != null) {
			builder.and(post.type.eq(command.type()));
		}
		if (command.includeDeleted() == null || !command.includeDeleted()) {
			builder.and(post.isDeleted.eq(false));
		}

		List<Post> content = jpaQueryFactory
			.selectFrom(post)
			.leftJoin(post.user).fetchJoin()
			.where(builder)
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize())
			.orderBy(post.createdAt.desc())
			.fetch();

		Long total = jpaQueryFactory
			.select(post.count())
			.from(post)
			.where(builder)
			.fetchOne();

		return new PageImpl<>(content, pageable, total != null ? total : 0L);
	}

	@Override
	public Optional<Post> findDeletedPostById(Long postId) {
		QPost post = QPost.post;

		Post result = jpaQueryFactory
			.selectFrom(post)
			.where(
				post.id.eq(postId),
				post.isDeleted.isTrue()
			)
			.fetchOne();
		return Optional.ofNullable(result);
	}
}
