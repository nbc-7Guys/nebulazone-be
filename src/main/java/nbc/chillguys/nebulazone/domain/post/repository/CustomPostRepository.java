package nbc.chillguys.nebulazone.domain.post.repository;

import java.util.Optional;

import nbc.chillguys.nebulazone.domain.post.entity.Post;

public interface CustomPostRepository {
	Optional<Post> findActivePostById(Long postId);

	Optional<Post> findActivePostByIdWithUserAndImages(Long postId);
}
