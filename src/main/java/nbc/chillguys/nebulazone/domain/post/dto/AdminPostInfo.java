package nbc.chillguys.nebulazone.domain.post.dto;

import java.time.LocalDateTime;

import nbc.chillguys.nebulazone.domain.post.entity.Post;

public record AdminPostInfo(
	Long postId,
	String title,
	String content,
	String nickname,
	nbc.chillguys.nebulazone.domain.post.entity.PostType type,
	boolean isDeleted,
	LocalDateTime deletedAt,
	LocalDateTime createdAt,
	LocalDateTime modifiedAt
) {
	public static AdminPostInfo from(Post post) {
		return new AdminPostInfo(
			post.getId(),
			post.getTitle(),
			post.getContent(),
			post.getUser().getNickname(),
			post.getType(),
			post.isDeleted(),
			post.getDeletedAt(),
			post.getCreatedAt(),
			post.getModifiedAt()
		);
	}
}
