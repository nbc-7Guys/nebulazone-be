package nbc.chillguys.nebulazone.domain.review.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import nbc.chillguys.nebulazone.domain.review.dto.ReviewAdminSearchQueryCommand;
import nbc.chillguys.nebulazone.domain.review.entity.Review;

public interface ReviewAdminRepositoryCustom {
	Page<Review> searchReviews(ReviewAdminSearchQueryCommand command, Pageable pageable);
}
