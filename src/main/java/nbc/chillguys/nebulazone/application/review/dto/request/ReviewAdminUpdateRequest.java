package nbc.chillguys.nebulazone.application.review.dto.request;

public record ReviewAdminUpdateRequest(
	String content,
	Integer star
) {
}
