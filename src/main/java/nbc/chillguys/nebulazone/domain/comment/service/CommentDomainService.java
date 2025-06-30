package nbc.chillguys.nebulazone.domain.comment.service;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.domain.comment.dto.CommentCreateCommand;
import nbc.chillguys.nebulazone.domain.comment.dto.CommentDeleteCommand;
import nbc.chillguys.nebulazone.domain.comment.dto.CommentListFindQuery;
import nbc.chillguys.nebulazone.domain.comment.dto.CommentUpdateCommand;
import nbc.chillguys.nebulazone.domain.comment.dto.CommentWithUserInfo;
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
	 * 댓글 생성
	 * @param command 댓글 생성 정보
	 * @return comment
	 * @author 윤정환
	 */
	@Transactional
	public Comment createComment(CommentCreateCommand command) {
		Comment comment = Comment.builder()
			.post(command.post())
			.user(command.user())
			.content(command.content())
			.build();

		commentRepository.save(comment);

		return comment;
	}

	/**
	 * 대댓글 생성
	 * @param command 댓글 생성 정보
	 * @return comment
	 * @author 윤정환
	 */
	@Transactional
	public Comment createChildComment(CommentCreateCommand command) {
		Comment parent = findActiveComment(command.parentId());
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
		return commentRepository.findByIdAndDeletedFalse(commentId)
			.orElseThrow(() -> new CommentException(CommentErrorCode.COMMENT_NOT_FOUND));
	}

	/**
	 * 뎃글 목록을 조회
	 * @param query 뎃글 조회 정보
	 * @return commentPage
	 * @author 윤정환
	 */
	public Page<CommentWithUserInfo> findComments(CommentListFindQuery query) {
		return commentRepository.findComments(query);
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

		comment.validateBelongsToPost(command.postId());
		comment.validateCommentOwner(command.userId());

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

		comment.validateBelongsToPost(command.postId());
		comment.validateCommentOwner(command.userId());

		comment.delete();
	}
}
