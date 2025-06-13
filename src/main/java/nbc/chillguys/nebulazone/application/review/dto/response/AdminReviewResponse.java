package nbc.chillguys.nebulazone.application.review.dto.response;

import java.time.LocalDateTime;

import nbc.chillguys.nebulazone.domain.review.dto.AdminReviewInfo;

public record AdminReviewResponse(
	Long reviewId,
	String content,
	int star,
	Long catalogId,
	String catalogName,
	LocalDateTime createdAt,
	LocalDateTime modifiedAt
) {
	public static AdminReviewResponse from(AdminReviewInfo info) {
		return new AdminReviewResponse(
			info.reviewId(),
			info.content(),
			info.star(),
			info.catalogId(),
			info.catalogName(),
			info.createdAt(),
			info.modifiedAt()
		);
	}
}
