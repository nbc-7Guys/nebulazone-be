package nbc.chillguys.nebulazone.domain.post.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import nbc.chillguys.nebulazone.domain.post.vo.PostDocument;

public interface PostEsRepositoryCustom {
	Page<PostDocument> searchPost(String keyword, String type, Pageable pageable);
}
