package nbc.chillguys.nebulazone.domain.post.repository;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.domain.post.entity.Post;
import nbc.chillguys.nebulazone.domain.post.entity.QPost;

@Repository
@RequiredArgsConstructor
public class PostRepositoryCustomImpl implements PostRepositoryCustom {
	private final JPAQueryFactory queryFactory;

	@Override
	public Optional<Post> findActivePostById(Long postId) {
		QPost post = QPost.post;

		return Optional.ofNullable(queryFactory.selectFrom(post)
			.where(
				post.isDeleted.eq(false),
				post.id.eq(postId)
			)
			.fetchOne());
	}

	@Override
	public Optional<Post> findActivePostByIdWithUser(Long postId) {
		QPost post = QPost.post;

		return Optional.ofNullable(queryFactory.selectFrom(post)
			.leftJoin(post.user).fetchJoin()
			.where(
				post.isDeleted.eq(false),
				post.id.eq(postId)
			)
			.fetchOne());
	}

	@Override
	public Optional<Post> findActivePostByIdWithUserAndImages(Long postId) {
		QPost post = QPost.post;

		return Optional.ofNullable(queryFactory.selectFrom(post)
			.leftJoin(post.user).fetchJoin()
			.leftJoin(post.postImages).fetchJoin()
			.where(
				post.isDeleted.eq(false),
				post.id.eq(postId)
			)
			.fetchOne());
	}

	@Override
	public boolean existActivePost(Long postId) {
		QPost post = QPost.post;

		return queryFactory.selectOne()
			.from(post)
			.where(post.isDeleted.eq(false), post.id.eq(postId))
			.fetchFirst() != null;
	}
}
