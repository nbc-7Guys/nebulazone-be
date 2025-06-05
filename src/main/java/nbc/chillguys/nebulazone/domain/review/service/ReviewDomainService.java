package nbc.chillguys.nebulazone.domain.review.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.domain.review.entity.Review;
import nbc.chillguys.nebulazone.domain.review.exception.ReviewErrorCode;
import nbc.chillguys.nebulazone.domain.review.exception.ReviewException;
import nbc.chillguys.nebulazone.domain.review.repository.ReviewRepository;

@Service
@RequiredArgsConstructor
public class ReviewDomainService {

	private final ReviewRepository reviewRepository;

	/**
	 * 카탈로그에 맞는 리뷰들 페이징 조회
	 *
	 * @param catalogId 카탈로그 ID
	 * @param pageable 페이징
	 * @return findReviews
	 * @author 정석현
	 */
	public Page<Review> findReviews(Long catalogId, Pageable pageable) {
		return reviewRepository.findReviews(catalogId, pageable);
	}

	/**
	 * 리뷰 ID로 리뷰를 조회합니다.
	 *
	 * @param reviewId 조회할 리뷰 ID
	 * @return 조회된 리뷰
	 * @throws ReviewException 리뷰가 존재하지 않을 경우
	 */
	public Review findActiveById(Long reviewId) {
		return reviewRepository.findById(reviewId)
			.orElseThrow(() -> new ReviewException(ReviewErrorCode.REVIEW_NOT_FOUND));
	}

}
