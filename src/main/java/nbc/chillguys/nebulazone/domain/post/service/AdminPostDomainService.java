package nbc.chillguys.nebulazone.domain.post.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.domain.post.dto.AdminPostInfo;
import nbc.chillguys.nebulazone.domain.post.dto.AdminPostSearchQueryCommand;
import nbc.chillguys.nebulazone.domain.post.repository.PostRepository;

@Service
@RequiredArgsConstructor
public class AdminPostDomainService {

	private final PostRepository postRepository;

	public Page<AdminPostInfo> findPosts(AdminPostSearchQueryCommand command, Pageable pageable) {
		return postRepository.searchPosts(command, pageable)
			.map(AdminPostInfo::from);
	}
}
