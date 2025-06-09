package nbc.chillguys.nebulazone.domain.post.service;

import java.util.List;
import java.util.Objects;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.domain.post.dto.PostCreateCommand;
import nbc.chillguys.nebulazone.domain.post.dto.PostDeleteCommand;
import nbc.chillguys.nebulazone.domain.post.dto.PostUpdateCommand;
import nbc.chillguys.nebulazone.domain.post.entity.Post;
import nbc.chillguys.nebulazone.domain.post.entity.PostType;
import nbc.chillguys.nebulazone.domain.post.exception.PostErrorCode;
import nbc.chillguys.nebulazone.domain.post.exception.PostException;
import nbc.chillguys.nebulazone.domain.post.repository.PostEsRepository;
import nbc.chillguys.nebulazone.domain.post.repository.PostRepository;
import nbc.chillguys.nebulazone.domain.post.vo.PostDocument;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostDomainService {

	private final PostRepository postRepository;
	private final PostEsRepository postEsRepository;

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

		validatePostOwner(post, command.userId());

		post.update(command.title(), command.content(), command.imageUrls());

		return post;
	}

	public Post findActivePost(Long postId) {
		return postRepository.findActivePostById(postId)
			.orElseThrow(() -> new PostException(PostErrorCode.POST_NOT_FOUND));
	}

	@Transactional
	public void deletePost(PostDeleteCommand command) {
		Post post = findActivePost(command.postId());

		validatePostOwner(post, command.userId());

		post.delete();
	}

	public Post findMyActivePost(Long postId, Long userId) {
		Post post = findActivePost(postId);

		validatePostOwner(post, userId);

		return post;
	}

	/**
	 * Elasticsearch에 게시글 저장
	 * @param post 게시글
	 * @author 이승현
	 */
	@Transactional
	public void savePostToEs(Post post) {
		postEsRepository.save(PostDocument.from(post));
	}

	/**
	 * 게시글 검색</br>
	 * keyword로 검색 제목, 본문을 토큰 단위로 검색, 유저명은 정확히 일치해야 함
	 * @param keyword 제목, 본문, 유저명
	 * @param type 게시글 유형
	 * @param page page idx
	 * @param size page size
	 * @return 게시글 목록
	 * @author 이승현
	 */
	public Page<PostDocument> searchPost(String keyword, PostType type, int page, int size) {
		Pageable pageable = PageRequest.of(page - 1, size);

		return postEsRepository.searchPost(keyword, type.name(), pageable);
	}

	/**
	 * 게시글 상세 조회</br>
	 * 유저와 이미지들도 함께 조회함
	 * @param postId 게시글 id
	 * @return post
	 * @author 이승현
	 */
	public Post getActivePostWithUserAndImages(Long postId) {
		return postRepository.findActivePostByIdWithUserAndImages(postId)
			.orElseThrow(() -> new PostException(PostErrorCode.POST_NOT_FOUND));
	}

	private void validatePostOwner(Post post, Long userId) {
		if (!Objects.equals(post.getUser().getId(), userId)) {
			throw new PostException(PostErrorCode.NOT_POST_OWNER);
		}
	}
}
