package nbc.chillguys.nebulazone.domain.comment.dto;

public record CommentAdminSearchQueryCommand(
	String keyword,
	Boolean deleted
) {
}
