package nbc.chillguys.nebulazone.domain.review.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.application.review.dto.request.ReviewAdminUpdateRequest;
import nbc.chillguys.nebulazone.domain.review.dto.ReviewAdminInfo;
import nbc.chillguys.nebulazone.domain.review.dto.ReviewAdminSearchQueryCommand;
import nbc.chillguys.nebulazone.domain.review.entity.Review;
import nbc.chillguys.nebulazone.domain.review.exception.ReviewErrorCode;
import nbc.chillguys.nebulazone.domain.review.exception.ReviewException;
import nbc.chillguys.nebulazone.domain.review.repository.ReviewRepository;

@Service
@RequiredArgsConstructor
public class ReviewAdminDomainService {
	private final ReviewRepository reviewRepository;

	@Transactional(readOnly = true)
	public Page<ReviewAdminInfo> findReviews(ReviewAdminSearchQueryCommand command, Pageable pageable) {
		return reviewRepository.searchReviews(command, pageable)
			.map(ReviewAdminInfo::from);
	}

	@Transactional
	public void updateReview(Long reviewId, ReviewAdminUpdateRequest request) {
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
