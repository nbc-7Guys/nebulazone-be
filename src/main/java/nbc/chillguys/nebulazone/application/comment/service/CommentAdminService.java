package nbc.chillguys.nebulazone.application.comment.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.application.comment.dto.request.CommentAdminSearchRequest;
import nbc.chillguys.nebulazone.application.comment.dto.request.CommentAdminUpdateRequest;
import nbc.chillguys.nebulazone.application.comment.dto.response.CommentAdminResponse;
import nbc.chillguys.nebulazone.common.response.CommonPageResponse;
import nbc.chillguys.nebulazone.domain.comment.dto.CommentAdminInfo;
import nbc.chillguys.nebulazone.domain.comment.dto.CommentAdminSearchQueryCommand;
import nbc.chillguys.nebulazone.domain.comment.service.CommentAdminDomainService;

@Service
@RequiredArgsConstructor
public class CommentAdminService {
	private final CommentAdminDomainService commentAdminDomainService;

	public CommonPageResponse<CommentAdminResponse> findComments(CommentAdminSearchRequest request, Pageable pageable) {
		CommentAdminSearchQueryCommand command = new CommentAdminSearchQueryCommand(
			request.keyword(),
			request.deleted()
		);
		Page<CommentAdminInfo> infoPage = commentAdminDomainService.findComments(command, pageable);
		return CommonPageResponse.from(infoPage.map(CommentAdminResponse::from));
	}

	public void updateComment(Long commentId, CommentAdminUpdateRequest request) {
		commentAdminDomainService.updateComment(commentId, request);
	}

	public void deleteComment(Long commentId) {
		commentAdminDomainService.deleteComment(commentId);
	}

	public void restoreComment(Long commentId) {
		commentAdminDomainService.restoreComment(commentId);
	}

}
