package nbc.chillguys.nebulazone.domain.comment.dto;

public record AdminCommentSearchQueryCommand(
	String keyword,
	Boolean deleted
) {
}
