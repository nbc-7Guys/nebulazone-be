package nbc.chillguys.nebulazone.domain.comment.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.domain.comment.dto.CommentCreateCommand;
import nbc.chillguys.nebulazone.domain.comment.entity.Comment;
import nbc.chillguys.nebulazone.domain.comment.exception.CommentErrorCode;
import nbc.chillguys.nebulazone.domain.comment.exception.CommentException;
import nbc.chillguys.nebulazone.domain.comment.repository.CommentRepository;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class CommentDomainService {

	private final CommentRepository commentRepository;

	@Transactional
	public Comment createComment(CommentCreateCommand command) {
		Comment parent = null;
		if (command.parentId() != null) {
			parent = findActiveComment(command.parentId());
		}

		Comment comment = Comment.builder()
			.post(command.post())
			.user(command.user())
			.content(command.content())
			.parent(parent)
			.build();

		commentRepository.save(comment);

		return comment;
	}

	public Comment findActiveComment(Long commentId) {
		return commentRepository.findById(commentId)
			.orElseThrow(() -> new CommentException(CommentErrorCode.COMMENT_NOT_FOUND));
	}
}
