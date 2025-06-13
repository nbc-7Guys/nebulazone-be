package nbc.chillguys.nebulazone.application.review.dto.request;

public record ReviewAdminSearchRequest(
	String keyword,
	Boolean isDeleted,
	int page,
	int size
) {
}
