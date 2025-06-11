package nbc.chillguys.nebulazone.domain.review.dto;

import java.time.LocalDateTime;

import nbc.chillguys.nebulazone.domain.review.entity.Review;

public record AdminReviewInfo(
	Long reviewId,
	String content,
	int star,
	Long catalogId,
	String catalogName,
	LocalDateTime createdAt,
	LocalDateTime modifiedAt
) {
	public static AdminReviewInfo from(Review review) {
		return new AdminReviewInfo(
			review.getId(),
			review.getContent(),
			review.getStar(),
			review.getCatalog().getId(),
			review.getCatalog().getName(),
			review.getCreatedAt(),
			review.getModifiedAt()
		);
	}
}
