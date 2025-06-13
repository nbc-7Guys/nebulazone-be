package nbc.chillguys.nebulazone.application.review.dto.response;

import java.time.LocalDateTime;

import nbc.chillguys.nebulazone.domain.review.entity.Review;

public record ReviewResponse(
	Long id,
	String content,
	Integer star,
	LocalDateTime createdAt
) {
	public static ReviewResponse from(Review review) {
		return new ReviewResponse(
			review.getId(),
			review.getContent(),
			review.getStar(),
			review.getCreatedAt()
		);
	}
}
