package nbc.chillguys.nebulazone.application.post.dto.response;

import java.time.LocalDateTime;

import nbc.chillguys.nebulazone.domain.post.dto.AdminPostInfo;
import nbc.chillguys.nebulazone.domain.post.entity.PostType;

public record AdminPostResponse(
	Long postId,
	String title,
	String nickname,
	PostType type,
	boolean isDeleted,
	LocalDateTime deletedAt,
	LocalDateTime createdAt,
	LocalDateTime modifiedAt
) {
	public static AdminPostResponse from(AdminPostInfo info) {
		return new AdminPostResponse(
			info.postId(),
			info.title(),
			info.nickname(),
			info.type(),
			info.isDeleted(),
			info.deletedAt(),
			info.createdAt(),
			info.modifiedAt()
		);
	}
}
