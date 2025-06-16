package nbc.chillguys.nebulazone.domain.post.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.domain.post.dto.PostAdminInfo;
import nbc.chillguys.nebulazone.domain.post.dto.PostAdminSearchQueryCommand;
import nbc.chillguys.nebulazone.domain.post.dto.PostAdminUpdateCommand;
import nbc.chillguys.nebulazone.domain.post.entity.Post;
import nbc.chillguys.nebulazone.domain.post.entity.PostType;
import nbc.chillguys.nebulazone.domain.post.exception.PostErrorCode;
import nbc.chillguys.nebulazone.domain.post.exception.PostException;
import nbc.chillguys.nebulazone.domain.post.repository.PostEsRepository;
import nbc.chillguys.nebulazone.domain.post.repository.PostRepository;
import nbc.chillguys.nebulazone.domain.post.vo.PostDocument;

@Service
@RequiredArgsConstructor
public class PostAdminDomainService {

	private final PostRepository postRepository;
	private final PostEsRepository postEsRepository;

	@Transactional(readOnly = true)
	public Page<PostAdminInfo> findPosts(PostAdminSearchQueryCommand command, Pageable pageable) {
		return postRepository.searchPosts(command, pageable)
			.map(PostAdminInfo::from);
	}

	@Transactional
	public Post updatePost(PostAdminUpdateCommand command) {
		Post post = findActivePost(command.postId());

		post.update(command.title(), command.content(), command.imageUrls());
		savePostToEs(post);

		return post;
	}

	@Transactional(readOnly = true)
	public Post getActivePostWithUserAndImages(Long postId) {
		return postRepository.findActivePostByIdWithUserAndImages(postId)
			.orElseThrow(() -> new PostException(PostErrorCode.POST_NOT_FOUND));
	}

	@Transactional
	public void updatePostType(Long postId, PostType type) {
		Post post = findActivePost(postId);
		post.updateType(type);
		savePostToEs(post);
	}

	@Transactional
	public void deletePost(Long postId) {
		Post post = findDeletedPost(postId);

		post.validatePostOwner(postId);

		post.delete();
		deletePostFromEs(postId);
	}

	@Transactional
	public void restorePost(Long postId) {
		Post post = findActivePost(postId);
		post.restore();
		savePostToEs(post);
	}

	public Post findActivePost(Long postId) {
		return postRepository.findActivePostById(postId)
			.orElseThrow(() -> new PostException(PostErrorCode.POST_NOT_FOUND));
	}

	public Post findMyActivePost(Long postId) {

		return findActivePost(postId);
	}

	public Post findDeletedPost(Long postId) {
		return postRepository.findDeletedPostById(postId)
			.orElseThrow(() -> new PostException(PostErrorCode.POST_NOT_FOUND));
	}

	@Transactional
	public void savePostToEs(Post post) {
		postEsRepository.save(PostDocument.from(post));
	}

	@Transactional
	public void deletePostFromEs(Long postId) {
		postEsRepository.deleteById(postId);
	}

}
