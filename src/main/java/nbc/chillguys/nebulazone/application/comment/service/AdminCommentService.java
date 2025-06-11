package nbc.chillguys.nebulazone.application.comment.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.application.comment.dto.request.AdminCommentSearchRequest;
import nbc.chillguys.nebulazone.application.comment.dto.request.AdminCommentUpdateRequest;
import nbc.chillguys.nebulazone.application.comment.dto.response.AdminCommentResponse;
import nbc.chillguys.nebulazone.common.response.CommonPageResponse;
import nbc.chillguys.nebulazone.domain.comment.dto.AdminCommentInfo;
import nbc.chillguys.nebulazone.domain.comment.dto.AdminCommentSearchQueryCommand;
import nbc.chillguys.nebulazone.domain.comment.service.AdminCommentDomainService;

@Service
@RequiredArgsConstructor
public class AdminCommentService {
	private final AdminCommentDomainService adminCommentDomainService;

	public CommonPageResponse<AdminCommentResponse> findComments(AdminCommentSearchRequest request, Pageable pageable) {
		AdminCommentSearchQueryCommand command = new AdminCommentSearchQueryCommand(
			request.keyword(),
			request.deleted()
		);
		Page<AdminCommentInfo> infoPage = adminCommentDomainService.findComments(command, pageable);
		return CommonPageResponse.from(infoPage.map(AdminCommentResponse::from));
	}

	public void updateComment(Long commentId, AdminCommentUpdateRequest request) {
		adminCommentDomainService.updateComment(commentId, request);
	}

	public void deleteComment(Long commentId) {
		adminCommentDomainService.deleteComment(commentId);
	}

}
