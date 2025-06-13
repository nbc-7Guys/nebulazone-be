package nbc.chillguys.nebulazone.application.review.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.application.review.dto.request.ReviewAdminSearchRequest;
import nbc.chillguys.nebulazone.application.review.dto.request.ReviewAdminUpdateRequest;
import nbc.chillguys.nebulazone.application.review.dto.response.ReviewAdminResponse;
import nbc.chillguys.nebulazone.common.response.CommonPageResponse;
import nbc.chillguys.nebulazone.domain.review.dto.ReviewAdminInfo;
import nbc.chillguys.nebulazone.domain.review.dto.ReviewAdminSearchQueryCommand;
import nbc.chillguys.nebulazone.domain.review.service.ReviewAdminDomainService;

@Service
@RequiredArgsConstructor
public class ReviewAdminService {
	private final ReviewAdminDomainService reviewAdminDomainService;

	public CommonPageResponse<ReviewAdminResponse> findReviews(ReviewAdminSearchRequest request, Pageable pageable) {
		ReviewAdminSearchQueryCommand command = new ReviewAdminSearchQueryCommand(
			request.keyword()
		);
		Page<ReviewAdminInfo> infoPage = reviewAdminDomainService.findReviews(command, pageable);
		return CommonPageResponse.from(infoPage.map(ReviewAdminResponse::from));
	}

	public void updateReview(Long reviewId, ReviewAdminUpdateRequest request) {
		reviewAdminDomainService.updateReview(reviewId, request);
	}

	public void deleteReview(Long reviewId) {
		reviewAdminDomainService.deleteReview(reviewId);
	}

}
