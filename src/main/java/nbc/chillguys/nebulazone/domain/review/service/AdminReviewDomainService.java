package nbc.chillguys.nebulazone.domain.review.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.application.review.dto.request.AdminReviewUpdateRequest;
import nbc.chillguys.nebulazone.domain.review.dto.AdminReviewInfo;
import nbc.chillguys.nebulazone.domain.review.dto.AdminReviewSearchQueryCommand;
import nbc.chillguys.nebulazone.domain.review.entity.Review;
import nbc.chillguys.nebulazone.domain.review.exception.ReviewErrorCode;
import nbc.chillguys.nebulazone.domain.review.exception.ReviewException;
import nbc.chillguys.nebulazone.domain.review.repository.ReviewRepository;

@Service
@RequiredArgsConstructor
public class AdminReviewDomainService {
	private final ReviewRepository reviewRepository;

	@Transactional(readOnly = true)
	public Page<AdminReviewInfo> findReviews(AdminReviewSearchQueryCommand command, Pageable pageable) {
		return reviewRepository.searchReviews(command, pageable)
			.map(AdminReviewInfo::from);
	}

	@Transactional
	public void updateReview(Long reviewId, AdminReviewUpdateRequest request) {
		Review review = reviewRepository.findById(reviewId)
			.orElseThrow(() -> new ReviewException(ReviewErrorCode.REVIEW_NOT_FOUND));
		review.update(request.content(), request.star());
	}

	@Transactional
	public void deleteReview(Long reviewId) {

		Review review = findById(reviewId);
		reviewRepository.delete(review);
	}

	public Review findById(Long reviewId) {
		return reviewRepository.findById(reviewId)
			.orElseThrow(() -> new ReviewException(ReviewErrorCode.REVIEW_NOT_FOUND));
	}

}
