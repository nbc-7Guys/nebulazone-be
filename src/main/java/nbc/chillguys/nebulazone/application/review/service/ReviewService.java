package nbc.chillguys.nebulazone.application.review.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.application.review.dto.response.ReviewResponse;
import nbc.chillguys.nebulazone.common.response.CommonPageResponse;
import nbc.chillguys.nebulazone.domain.review.entity.Review;
import nbc.chillguys.nebulazone.domain.review.service.ReviewDomainService;

@Service
@RequiredArgsConstructor
public class ReviewService {

	private final ReviewDomainService reviewDomainService;

	@Transactional(readOnly = true)
	public CommonPageResponse<ReviewResponse> findReviews(Long catalogId, int page, int size) {
		Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
		Page<Review> reviews = reviewDomainService.findReviews(catalogId, pageable);
		Page<ReviewResponse> dtoPage = reviews.map(ReviewResponse::from);
		return CommonPageResponse.from(dtoPage);
	}
}
