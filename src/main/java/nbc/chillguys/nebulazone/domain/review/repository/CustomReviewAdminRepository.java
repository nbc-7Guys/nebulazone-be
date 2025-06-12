package nbc.chillguys.nebulazone.domain.review.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import nbc.chillguys.nebulazone.domain.review.dto.AdminReviewSearchQueryCommand;
import nbc.chillguys.nebulazone.domain.review.entity.Review;

public interface CustomReviewAdminRepository {
	Page<Review> searchReviews(AdminReviewSearchQueryCommand command, Pageable pageable);
}
