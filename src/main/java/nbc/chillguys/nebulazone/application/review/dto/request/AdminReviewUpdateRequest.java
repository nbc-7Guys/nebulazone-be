package nbc.chillguys.nebulazone.application.review.dto.request;

public record AdminReviewUpdateRequest(
	String content,
	Integer star
) {
}
