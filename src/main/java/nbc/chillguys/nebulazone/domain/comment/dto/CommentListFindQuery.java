package nbc.chillguys.nebulazone.domain.comment.dto;

import nbc.chillguys.nebulazone.domain.post.entity.Post;

public record CommentListFindQuery(
	Post post,
	Integer page,
	Integer size
) {

	public static CommentListFindQuery of(Post post, int page, int size) {
		return new CommentListFindQuery(post, page, size);
	}
}
