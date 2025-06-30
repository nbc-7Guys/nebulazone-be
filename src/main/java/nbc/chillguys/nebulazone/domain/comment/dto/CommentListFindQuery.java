package nbc.chillguys.nebulazone.domain.comment.dto;

public record CommentListFindQuery(
	Long postId,
	Integer page,
	Integer size
) {

	public static CommentListFindQuery of(Long postId, int page, int size) {
		return new CommentListFindQuery(postId, page, size);
	}
}
