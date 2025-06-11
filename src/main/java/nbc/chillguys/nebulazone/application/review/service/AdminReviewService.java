package nbc.chillguys.nebulazone.application.review.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.application.review.dto.request.AdminReviewSearchRequest;
import nbc.chillguys.nebulazone.application.review.dto.response.AdminReviewResponse;
import nbc.chillguys.nebulazone.common.response.CommonPageResponse;
import nbc.chillguys.nebulazone.domain.review.dto.AdminReviewInfo;
import nbc.chillguys.nebulazone.domain.review.dto.AdminReviewSearchQueryCommand;
import nbc.chillguys.nebulazone.domain.review.service.AdminReviewDomainService;

@Service
@RequiredArgsConstructor
public class AdminReviewService {
	private final AdminReviewDomainService adminReviewDomainService;

	public CommonPageResponse<AdminReviewResponse> findReviews(AdminReviewSearchRequest request, Pageable pageable) {
		AdminReviewSearchQueryCommand command = new AdminReviewSearchQueryCommand(
			request.keyword()
		);
		Page<AdminReviewInfo> infoPage = adminReviewDomainService.findReviews(command, pageable);
		return CommonPageResponse.from(infoPage.map(AdminReviewResponse::from));
	}
}
