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

	@Transactional(readOnly = true)
	public Page<CommentAdminInfo> findComments(CommentAdminSearchQueryCommand command, Pageable pageable) {
		return commentRepository.searchComments(command, pageable)
			.map(CommentAdminInfo::from);
	}

	@Transactional
	public void updateComment(Long commentId, CommentAdminUpdateRequest request) {
		Comment comment = findBycommentId(commentId);

		comment.update(request.content());
	}

	@Transactional
	public void deleteComment(Long commentId) {
		Comment comment = findBycommentId(commentId);

		comment.delete();
	}

	@Transactional
	public void restoreComment(Long commentId) {
		Comment comment = findBycommentId(commentId);

		comment.restore();
	}

	public Comment findBycommentId(Long commentId) {
		return commentRepository.findById(commentId)
			.orElseThrow(() -> new CommentException(CommentErrorCode.COMMENT_NOT_FOUND));
	}

}
