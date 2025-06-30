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

	/**
	 * 검색 조건과 페이징 정보에 따라 리뷰 목록을 조회합니다.
	 *
	 * @param command  리뷰 검색 조건
	 * @param pageable 페이징 정보
	 * @return 리뷰 정보 페이지
	 * @author 정석현
	 */
	@Transactional(readOnly = true)
	public Page<ReviewAdminInfo> findReviews(ReviewAdminSearchQueryCommand command, Pageable pageable) {
		return reviewRepository.searchReviews(command, pageable)
			.map(ReviewAdminInfo::from);
	}

	/**
	 * 리뷰 내용을 수정합니다.
	 *
	 * @param reviewId 수정할 리뷰의 ID
	 * @param request  수정 요청 데이터(내용, 별점 등)
	 * @author 정석현
	 */
	@Transactional
	public void updateReview(Long reviewId, ReviewAdminUpdateRequest request) {
		Review review = reviewRepository.findById(reviewId)
			.orElseThrow(() -> new ReviewException(ReviewErrorCode.REVIEW_NOT_FOUND));
		review.update(request.content(), request.star());
	}

	/**
	 * 리뷰를 삭제합니다.
	 *
	 * @param reviewId 삭제할 리뷰의 ID
	 * @author 정석현
	 */
	@Transactional
	public void deleteReview(Long reviewId) {

		Review review = findById(reviewId);
		reviewRepository.delete(review);
	}

	/**
	 * 리뷰 ID로 리뷰를 조회합니다.<br>
	 * 존재하지 않을 경우 예외를 발생시킵니다.
	 *
	 * @param reviewId 리뷰 ID
	 * @return 리뷰 엔티티
	 * @author 정석현
	 */
	public Review findById(Long reviewId) {
		return reviewRepository.findById(reviewId)
			.orElseThrow(() -> new ReviewException(ReviewErrorCode.REVIEW_NOT_FOUND));
	}

}
