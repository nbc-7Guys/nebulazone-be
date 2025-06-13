package nbc.chillguys.nebulazone.application.comment.dto.request;

public record CommentAdminSearchRequest(
	String keyword,
	Boolean deleted,
	int page,
	int size
) {
}
