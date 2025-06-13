package nbc.chillguys.nebulazone.domain.review.dto;

import java.time.LocalDateTime;

import nbc.chillguys.nebulazone.domain.review.entity.Review;

public record ReviewAdminInfo(
	Long reviewId,
	String content,
	int star,
	Long catalogId,
	String catalogName,
	LocalDateTime createdAt,
	LocalDateTime modifiedAt
) {
	public static ReviewAdminInfo from(Review review) {
		return new ReviewAdminInfo(
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
