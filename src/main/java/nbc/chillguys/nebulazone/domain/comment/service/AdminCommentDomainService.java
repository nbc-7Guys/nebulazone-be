package nbc.chillguys.nebulazone.domain.comment.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.application.comment.dto.request.AdminCommentUpdateRequest;
import nbc.chillguys.nebulazone.domain.comment.dto.AdminCommentInfo;
import nbc.chillguys.nebulazone.domain.comment.dto.AdminCommentSearchQueryCommand;
import nbc.chillguys.nebulazone.domain.comment.entity.Comment;
import nbc.chillguys.nebulazone.domain.comment.exception.CommentErrorCode;
import nbc.chillguys.nebulazone.domain.comment.exception.CommentException;
import nbc.chillguys.nebulazone.domain.comment.repository.CommentRepository;

@Service
@RequiredArgsConstructor
public class AdminCommentDomainService {
	private final CommentRepository commentRepository;

	@Transactional(readOnly = true)
	public Page<AdminCommentInfo> findComments(AdminCommentSearchQueryCommand command, Pageable pageable) {
		return commentRepository.searchComments(command, pageable)
			.map(AdminCommentInfo::from);
	}

	@Transactional
	public void updateComment(Long commentId, AdminCommentUpdateRequest request) {
		Comment comment = findBiCommentId(commentId);

		comment.update(request.content());
	}

	@Transactional
	public void deleteComment(Long commentId) {
		Comment comment = findBiCommentId(commentId);

		comment.delete();
	}

	@Transactional
	public void restoreComment(Long commentId) {
		Comment comment = findBiCommentId(commentId);

		comment.restore();
	}

	public Comment findBiCommentId(Long commentId) {
		return commentRepository.findById(commentId)
			.orElseThrow(() -> new CommentException(CommentErrorCode.COMMENT_NOT_FOUND));
	}

}
