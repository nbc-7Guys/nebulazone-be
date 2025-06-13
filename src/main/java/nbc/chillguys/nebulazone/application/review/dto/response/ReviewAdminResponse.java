package nbc.chillguys.nebulazone.application.review.dto.response;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import nbc.chillguys.nebulazone.domain.review.dto.ReviewAdminInfo;

public record ReviewAdminResponse(
	Long reviewId,
	String content,
	int star,
	Long catalogId,
	String catalogName,
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	LocalDateTime createdAt,
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	LocalDateTime modifiedAt
) {
	public static ReviewAdminResponse from(ReviewAdminInfo info) {
		return new ReviewAdminResponse(
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
