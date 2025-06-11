package nbc.chillguys.nebulazone.application.review.dto.request;

public record AdminReviewSearchRequest(
	String keyword,
	Boolean isDeleted,
	int page,
	int size
) {
}
