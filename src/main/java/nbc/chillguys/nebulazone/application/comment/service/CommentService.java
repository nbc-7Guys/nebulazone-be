package nbc.chillguys.nebulazone.application.comment.service;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.application.comment.dto.request.CreateCommentRequest;
import nbc.chillguys.nebulazone.application.comment.dto.request.UpdateCommentRequest;
import nbc.chillguys.nebulazone.application.comment.dto.response.CommentDetailResponse;
import nbc.chillguys.nebulazone.application.comment.dto.response.CommentResponse;
import nbc.chillguys.nebulazone.application.comment.dto.response.DeleteCommentResponse;
import nbc.chillguys.nebulazone.common.response.CommonPageResponse;
import nbc.chillguys.nebulazone.domain.comment.dto.CommentCreateCommand;
import nbc.chillguys.nebulazone.domain.comment.dto.CommentDeleteCommand;
import nbc.chillguys.nebulazone.domain.comment.dto.CommentListFindQuery;
import nbc.chillguys.nebulazone.domain.comment.dto.CommentUpdateCommand;
import nbc.chillguys.nebulazone.domain.comment.dto.CommentWithUserInfo;
import nbc.chillguys.nebulazone.domain.comment.entity.Comment;
import nbc.chillguys.nebulazone.domain.comment.service.CommentDomainService;
import nbc.chillguys.nebulazone.domain.post.entity.Post;
import nbc.chillguys.nebulazone.domain.post.service.PostDomainService;
import nbc.chillguys.nebulazone.domain.user.entity.User;

@RequiredArgsConstructor
@Service
public class CommentService {

	public static final int COMMENT_SIZE_PER_PAGE = 20;

	private final PostDomainService postDomainService;
	private final CommentDomainService commentDomainService;

	public CommentResponse createComment(User user, Long postId, CreateCommentRequest request) {
		Post post = postDomainService.findActivePost(postId);

		CommentCreateCommand command = request.toCommand(user, post);
		Comment comment;
		if (command.parentId() > 0) {
			comment = commentDomainService.createChildComment(command);
		} else {
			comment = commentDomainService.createComment(command);
		}

		return CommentResponse.from(comment);
	}

	public CommonPageResponse<CommentDetailResponse> findComments(Long postId, int page, int size) {
		if (page < 1) {
			page = 1;
		}
		if (size < 0 || size > COMMENT_SIZE_PER_PAGE) {
			size = COMMENT_SIZE_PER_PAGE;
		}

		Post post = postDomainService.findActivePost(postId);

		CommentListFindQuery query = CommentListFindQuery.of(postId, page - 1, size);
		Page<CommentWithUserInfo> comments = commentDomainService.findComments(query);
		Page<CommentDetailResponse> response = comments.map(CommentDetailResponse::from);

		return CommonPageResponse.from(response);
	}

	public CommentResponse updateComment(Long commentId, Long userId, Long postId, UpdateCommentRequest request) {
		postDomainService.validActivePost(postId);

		CommentUpdateCommand command = request.toCommand(commentId, userId, postId);
		Comment comment = commentDomainService.updateComment(command);

		return CommentResponse.from(comment);
	}

	public DeleteCommentResponse deleteComment(Long commentId, Long userId, Long postId) {
		postDomainService.validActivePost(postId);

		CommentDeleteCommand command = CommentDeleteCommand.of(commentId, userId, postId);
		commentDomainService.deleteComment(command);

		return DeleteCommentResponse.from(commentId);
	}
}
