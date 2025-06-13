package nbc.chillguys.nebulazone.domain.post.dto;

import java.time.LocalDateTime;

import nbc.chillguys.nebulazone.domain.post.entity.Post;
import nbc.chillguys.nebulazone.domain.post.entity.PostType;

public record PostAdminInfo(
	Long postId,
	String title,
	String nickname,
	PostType type,
	boolean isDeleted,
	LocalDateTime deletedAt,
	LocalDateTime createdAt,
	LocalDateTime modifiedAt
) {
	public static PostAdminInfo from(Post post) {
		return new PostAdminInfo(
			post.getId(),
			post.getTitle(),
			post.getUser().getNickname(),
			post.getType(),
			post.isDeleted(),
			post.getDeletedAt(),
			post.getCreatedAt(),
			post.getModifiedAt()
		);
	}
}
