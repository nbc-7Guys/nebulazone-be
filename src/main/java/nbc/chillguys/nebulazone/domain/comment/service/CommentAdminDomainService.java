package nbc.chillguys.nebulazone.domain.comment.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.application.comment.dto.request.CommentAdminUpdateRequest;
import nbc.chillguys.nebulazone.domain.comment.dto.CommentAdminInfo;
import nbc.chillguys.nebulazone.domain.comment.dto.CommentAdminSearchQueryCommand;
import nbc.chillguys.nebulazone.domain.comment.entity.Comment;
import nbc.chillguys.nebulazone.domain.comment.exception.CommentErrorCode;
import nbc.chillguys.nebulazone.domain.comment.exception.CommentException;
import nbc.chillguys.nebulazone.domain.comment.repository.CommentRepository;

@Service
@RequiredArgsConstructor
public class CommentAdminDomainService {
	private final CommentRepository commentRepository;

	/**
	 * 검색 조건과 페이징 정보에 따라 댓글 목록을 조회합니다.
	 *
	 * @param command  댓글 검색 조건
	 * @param pageable 페이징 정보
	 * @return 댓글 정보 페이지
	 * @author 정석현
	 */
	@Transactional(readOnly = true)
	public Page<CommentAdminInfo> findComments(CommentAdminSearchQueryCommand command, Pageable pageable) {
		return commentRepository.searchComments(command, pageable)
			.map(CommentAdminInfo::from);
	}

	/**
	 * 댓글 내용을 수정합니다.
	 *
	 * @param commentId 수정할 댓글 ID
	 * @param request   수정 요청 데이터
	 * @author 정석현
	 */
	@Transactional
	public void updateComment(Long commentId, CommentAdminUpdateRequest request) {
		Comment comment = findByCommentId(commentId);

		comment.update(request.content());
	}

	/**
	 * 댓글을 삭제(소프트 딜리트) 처리합니다.
	 *
	 * @param commentId 삭제할 댓글 ID
	 * @author 정석현
	 */
	@Transactional
	public void deleteComment(Long commentId) {
		Comment comment = findByCommentId(commentId);

		comment.delete();
	}

	/**
	 * 삭제된 댓글을 복원(undo delete)합니다.
	 *
	 * @param commentId 복원할 댓글 ID
	 * @author 정석현
	 */
	@Transactional
	public void restoreComment(Long commentId) {
		Comment comment = findByCommentId(commentId);

		comment.restore();
	}

	/**
	 * 댓글 ID로 댓글을 조회합니다.
	 *
	 * @param commentId 댓글 ID
	 * @return 댓글 엔티티
	 * @author 정석현
	 */
	public Comment findByCommentId(Long commentId) {
		return commentRepository.findById(commentId)
			.orElseThrow(() -> new CommentException(CommentErrorCode.COMMENT_NOT_FOUND));
	}

}
