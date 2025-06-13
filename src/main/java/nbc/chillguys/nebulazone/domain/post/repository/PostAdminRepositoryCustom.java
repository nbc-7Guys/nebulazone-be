package nbc.chillguys.nebulazone.domain.post.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import nbc.chillguys.nebulazone.domain.post.dto.PostAdminSearchQueryCommand;
import nbc.chillguys.nebulazone.domain.post.entity.Post;

public interface PostAdminRepositoryCustom {
	Page<Post> searchPosts(PostAdminSearchQueryCommand command, Pageable pageable);

	Optional<Post> findDeletedPostById(Long postId);
}
