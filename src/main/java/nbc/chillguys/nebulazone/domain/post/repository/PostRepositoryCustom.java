package nbc.chillguys.nebulazone.domain.post.repository;

import java.util.Optional;

import nbc.chillguys.nebulazone.domain.post.entity.Post;

public interface PostRepositoryCustom {
	Optional<Post> findActivePostById(Long postId);

	Optional<Post> findActivePostByIdWithUser(Long postId);

	Optional<Post> findActivePostByIdWithUserAndImages(Long postId);
}
