package nbc.chillguys.nebulazone.domain.post.dto;

public record PostAdminUpdateCommand(
	Long postId,
	String title,
	String content) {
}
