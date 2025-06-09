package nbc.chillguys.nebulazone.domain.post.service;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.domain.post.dto.PostCreateCommand;
import nbc.chillguys.nebulazone.domain.post.dto.PostDeleteCommand;
import nbc.chillguys.nebulazone.domain.post.dto.PostUpdateCommand;
import nbc.chillguys.nebulazone.domain.post.entity.Post;
import nbc.chillguys.nebulazone.domain.post.exception.PostErrorCode;
import nbc.chillguys.nebulazone.domain.post.exception.PostException;
import nbc.chillguys.nebulazone.domain.post.repository.PostRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostDomainService {

	private final PostRepository postRepository;

	/**
	 * 게시글 생성
	 * @param command 유저, 게시글 제목, 게시글 내용, 게시판 종류
	 * @param postImageUrls 게시글 이미지 리스트
	 * @return Post
	 * @author 전나겸
	 */
	@Transactional
	public Post createPost(PostCreateCommand command, List<String> postImageUrls) {

		Post post = Post.builder()
			.title(command.title())
			.content(command.content())
			.type(command.type())
			.user(command.user())
			.build();

		post.addPostImages(postImageUrls);

		return postRepository.save(post);
	}

	@Transactional
	public Post updatePost(PostUpdateCommand command) {
		Post post = findActivePost(command.postId());

		post.validatePostOwner(command.userId());

		post.update(command.title(), command.content(), command.imageUrls());

		return post;
	}

	public Post findActivePost(Long postId) {
		return postRepository.findById(postId)
			.orElseThrow(() -> new PostException(PostErrorCode.POST_NOT_FOUND));
	}

	@Transactional
	public void deletePost(PostDeleteCommand command) {
		Post post = findActivePost(command.postId());

		post.validatePostOwner(command.userId());

		post.delete();
	}

	public Post findMyActivePost(Long postId, Long userId) {
		Post post = findActivePost(postId);

		post.validatePostOwner(userId);

		return post;
	}
}
