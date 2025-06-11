package nbc.chillguys.nebulazone.application.comment.dto.request;

public record AdminCommentSearchRequest(
	String keyword,
	Boolean deleted,
	int page,
	int size
) {
}
