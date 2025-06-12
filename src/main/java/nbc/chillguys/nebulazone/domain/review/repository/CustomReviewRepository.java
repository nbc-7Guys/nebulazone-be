package nbc.chillguys.nebulazone.domain.review.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import nbc.chillguys.nebulazone.domain.review.entity.Review;

public interface CustomReviewRepository {
	Page<Review> findReviews(Long catalogId, Pageable pageable);
}
