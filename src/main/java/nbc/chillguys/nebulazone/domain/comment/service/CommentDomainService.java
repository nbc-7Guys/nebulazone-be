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

	/**
	 * 댓글 혹은 대댓글 생성
	 * @param command 댓글 생성 정보
	 * @return comment
	 * @author 윤정환
	 */
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

	/**
	 * 삭제되지 않은 댓글 단건 조회
	 * @param commentId 댓글 id
	 * @return comment
	 * @author 윤정환
	 */
	public Comment findActiveComment(Long commentId) {
		return commentRepository.findById(commentId)
			.orElseThrow(() -> new CommentException(CommentErrorCode.COMMENT_NOT_FOUND));
	}

	/**
	 * 뎃글 목록을 조회
	 * @param query 뎃글 조회 정보
	 * @return commentPage
	 * @author 윤정환
	 */
	public Page<CommentWithUserDto> findComments(CommentListFindQuery query) {
		return commentRepository.findComments(query.post().getId(), query.page(), query.size());
	}

	/**
	 * 뎃글 수정
	 * @param command 댓글 수정 정보
	 * @return comment
	 * @author 윤정환
	 */
	@Transactional
	public Comment updateComment(CommentUpdateCommand command) {
		Comment comment = findActiveComment(command.commentId());

		validateBelongsToPost(comment, command.post().getId());
		validateCommentOwner(comment, command.user().getId());

		comment.update(command.content());

		return comment;
	}

	/**
	 * 댓글 삭제
	 * @param command 댓글 삭제 정보
	 * @author 윤정환
	 */
	@Transactional
	public void deleteComment(CommentDeleteCommand command) {
		Comment comment = findActiveComment(command.commentId());

		validateBelongsToPost(comment, command.post().getId());
		validateCommentOwner(comment, command.user().getId());

		comment.delete();
	}

	/**
	 * 댓글이 해당 게시글에 속해있는지 검증
	 * @param comment 뎃글 정보
	 * @param postId 게시글 id
	 * @author 윤정환
	 */
	private void validateBelongsToPost(Comment comment, Long postId) {
		if (!Objects.equals(comment.getPost().getId(), postId)) {
			throw new CommentException(CommentErrorCode.NOT_BELONG_TO_POST);
		}
	}

	/**
	 * 댓글의 주인이 맞는지 검증
	 * @param comment 뎃글 정보
	 * @param userId 유저 id
	 */
	private void validateCommentOwner(Comment comment, Long userId) {
		if (!Objects.equals(comment.getUser().getId(), userId)) {
			throw new CommentException(CommentErrorCode.NOT_COMMENT_OWNER);
		}
	}
}
