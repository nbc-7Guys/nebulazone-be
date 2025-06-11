package nbc.chillguys.nebulazone.domain.post.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import nbc.chillguys.nebulazone.domain.post.dto.AdminPostSearchQueryCommand;
import nbc.chillguys.nebulazone.domain.post.entity.Post;

public interface CustomPostAdminRepository {
	Page<Post> searchPosts(AdminPostSearchQueryCommand command, Pageable pageable);

	Optional<Post> findDeletedPostById(Long postId);
}
