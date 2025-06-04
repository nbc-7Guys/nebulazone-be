package nbc.chillguys.nebulazone.domain.comment.service;

import java.util.Objects;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.domain.comment.dto.CommentCreateCommand;
import nbc.chillguys.nebulazone.domain.comment.dto.CommentDeleteCommand;
import nbc.chillguys.nebulazone.domain.comment.dto.CommentListFindQuery;
import nbc.chillguys.nebulazone.domain.comment.dto.CommentUpdateCommand;
import nbc.chillguys.nebulazone.domain.comment.dto.CommentWithUserDto;
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

	public Page<CommentWithUserDto> findComments(CommentListFindQuery query) {
		return commentRepository.findComments(query.post().getId(), query.page(), query.size());
	}

	@Transactional
	public Comment updateComment(CommentUpdateCommand command) {
		Comment comment = findActiveComment(command.commentId());

		validateBelongsToPost(comment, command.post().getId());
		validateCommentOwner(comment, command.user().getId());

		comment.update(command.content());

		return comment;
	}

	@Transactional
	public void deleteComment(CommentDeleteCommand command) {
		Comment comment = findActiveComment(command.commentId());

		validateBelongsToPost(comment, command.post().getId());
		validateCommentOwner(comment, command.user().getId());

		comment.delete();
	}


	private void validateBelongsToPost(Comment comment, Long postId) {
		if (!Objects.equals(comment.getPost().getId(), postId)) {
			throw new CommentException(CommentErrorCode.NOT_BELONG_TO_POST);
		}
	}

	private void validateCommentOwner(Comment comment, Long userId) {
		if (!Objects.equals(comment.getUser().getId(), userId)) {
			throw new CommentException(CommentErrorCode.NOT_COMMENT_OWNER);
		}
	}
}
