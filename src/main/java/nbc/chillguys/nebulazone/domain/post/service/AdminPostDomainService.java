package nbc.chillguys.nebulazone.domain.post.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.domain.post.dto.AdminPostInfo;
import nbc.chillguys.nebulazone.domain.post.dto.AdminPostSearchQueryCommand;
import nbc.chillguys.nebulazone.domain.post.dto.AdminPostUpdateCommand;
import nbc.chillguys.nebulazone.domain.post.entity.Post;
import nbc.chillguys.nebulazone.domain.post.exception.PostErrorCode;
import nbc.chillguys.nebulazone.domain.post.exception.PostException;
import nbc.chillguys.nebulazone.domain.post.repository.PostEsRepository;
import nbc.chillguys.nebulazone.domain.post.repository.PostRepository;
import nbc.chillguys.nebulazone.domain.post.vo.PostDocument;

@Service
@RequiredArgsConstructor
public class AdminPostDomainService {

	private final PostRepository postRepository;
	private final PostEsRepository postEsRepository;

	public Page<AdminPostInfo> findPosts(AdminPostSearchQueryCommand command, Pageable pageable) {
		return postRepository.searchPosts(command, pageable)
			.map(AdminPostInfo::from);
	}

	@Transactional
	public Post updatePost(AdminPostUpdateCommand command) {
		Post post = findActivePost(command.postId());

		post.update(command.title(), command.content(), command.imageUrls());

		return post;
	}

	public Post findMyActivePost(Long postId) {

		return findActivePost(postId);
	}

	public Post getActivePostWithUserAndImages(Long postId) {
		return postRepository.findActivePostByIdWithUserAndImages(postId)
			.orElseThrow(() -> new PostException(PostErrorCode.POST_NOT_FOUND));
	}

	public Post findActivePost(Long postId) {
		return postRepository.findActivePostById(postId)
			.orElseThrow(() -> new PostException(PostErrorCode.POST_NOT_FOUND));
	}

	@Transactional
	public void savePostToEs(Post post) {
		postEsRepository.save(PostDocument.from(post));
	}

}
