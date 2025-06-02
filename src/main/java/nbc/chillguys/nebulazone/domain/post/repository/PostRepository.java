package nbc.chillguys.nebulazone.domain.post.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import nbc.chillguys.nebulazone.domain.post.entity.Post;

public interface PostRepository extends JpaRepository<Post, Long> {
	Optional<Post> findByUserId(Long userId);
}
