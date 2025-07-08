package nbc.chillguys.nebulazone.domain.post.service;

import java.util.List;

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
import nbc.chillguys.nebulazone.domain.post.repository.PostRepository;

@Service
@RequiredArgsConstructor
public class PostAdminDomainService {

	private final PostRepository postRepository;

	/**
	 * 검색 조건과 페이징 정보에 따라 게시글 목록을 조회합니다.
	 *
	 * @param command  게시글 검색 조건
	 * @param pageable 페이징 정보
	 * @return 게시글 정보 페이지
	 * @author 정석현
	 */
	@Transactional(readOnly = true)
	public Page<PostAdminInfo> findPosts(PostAdminSearchQueryCommand command, Pageable pageable) {
		return postRepository.searchPosts(command, pageable)
			.map(PostAdminInfo::from);
	}

	/**
	 * 게시글 정보를 수정합니다.<br>
	 * - 제목, 내용, 이미지 변경<br>
	 * - 수정 후 ES(검색엔진) 인덱스에 반영합니다.
	 *
	 * @param command 수정 요청 데이터
	 * @return 수정된 게시글 엔티티
	 * @author 정석현
	 */
	@Transactional
	public Post updatePost(PostAdminUpdateCommand command) {
		Post post = findActivePost(command.postId());

		post.update(command.title(), command.content());
		return post;
	}

	/**
	 * 게시글, 작성자, 이미지 정보를 조인하여 조회합니다.
	 *
	 * @param postId 조회할 게시글 ID
	 * @return 게시글 엔티티
	 * @author 정석현
	 */
	@Transactional(readOnly = true)
	public Post getActivePostWithUserAndImages(Long postId) {
		return postRepository.findActivePostByIdWithUserAndImages(postId)
			.orElseThrow(() -> new PostException(PostErrorCode.POST_NOT_FOUND));
	}

	/**
	 * 게시글의 유형(PostType)을 변경합니다.<br>
	 * 변경 후 ES(검색엔진) 인덱스에 반영합니다.
	 *
	 * @param postId 게시글 ID
	 * @param type   변경할 게시글 유형
	 * @author 정석현
	 */
	@Transactional
	public Post updatePostType(Long postId, PostType type) {
		Post post = findActivePost(postId);
		post.updateType(type);
		return post;
	}

	/**
	 * 게시글을 삭제 처리(소프트 딜리트)합니다.<br>
	 * - 소유자 검증 로직 포함<br>
	 * - 삭제 후 ES(검색엔진)에서도 제거
	 *
	 * @param postId 삭제할 게시글 ID
	 * @author 정석현
	 */
	@Transactional
	public Long deletePost(Long postId) {
		Post post = findDeletedPost(postId);

		post.validatePostOwner(postId);

		post.delete();
		return post.getId();
	}

	/**
	 * 삭제된 게시글을 복원(undo delete)합니다.<br>
	 * 복원 후 ES(검색엔진)에 반영합니다.
	 *
	 * @param postId 복원할 게시글 ID
	 * @author 정석현
	 */
	@Transactional
	public Post restorePost(Long postId) {
		Post post = findActivePost(postId);
		post.restore();
		return post;
	}

	/**
	 * 내 게시글 중 활성 상태인 게시글을 조회합니다.
	 *
	 * @param postId 게시글 ID
	 * @return 게시글 엔티티
	 * @author 정석현
	 */
	public Post findActivePost(Long postId) {
		return postRepository.findActivePostById(postId)
			.orElseThrow(() -> new PostException(PostErrorCode.POST_NOT_FOUND));
	}

	/**
	 * 삭제된 게시글을 ID로 조회합니다.
	 *
	 * @param postId 게시글 ID
	 * @return 게시글 엔티티
	 * @author 정석현
	 */
	public Post findDeletedPost(Long postId) {
		return postRepository.findDeletedPostById(postId)
			.orElseThrow(() -> new PostException(PostErrorCode.POST_NOT_FOUND));
	}

	/**
	 * 게시글의 이미지 업데이트
	 *
	 * @param post 업데이트할 이미지
	 * @param postImageUrls 이미지 url 리스트
	 * @return 업데이트된 Post
	 * @author 전나겸
	 */
	public Post updatePostImages(Post post, List<String> postImageUrls) {

		post.updatePostImages(postImageUrls);

		return post;
	}
}
