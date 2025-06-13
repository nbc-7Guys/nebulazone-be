package nbc.chillguys.nebulazone.domain.post.dto;

public record PostDeleteCommand(
	Long userId,
	Long postId
) {

	public static PostDeleteCommand of(Long userId, Long postId) {
		return new PostDeleteCommand(userId, postId);
	}
}
