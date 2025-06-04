package nbc.chillguys.nebulazone.application.review.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.application.review.dto.response.ReviewResponse;
import nbc.chillguys.nebulazone.domain.review.service.ReviewDomainService;

@Service
@RequiredArgsConstructor
public class ReviewService {

	private final ReviewDomainService reviewDomainService;

	@Transactional(readOnly = true)
	public Page<ReviewResponse> findReviews(Long catalogId, Pageable pageable) {
		return reviewDomainService.findReviews(catalogId, pageable).map(ReviewResponse::from);
	}
}
