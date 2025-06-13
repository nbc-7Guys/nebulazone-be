package nbc.chillguys.nebulazone.application.post.dto.response;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import nbc.chillguys.nebulazone.domain.post.dto.PostAdminInfo;
import nbc.chillguys.nebulazone.domain.post.entity.PostType;

public record PostAdminResponse(
	Long postId,
	String title,
	String nickname,
	PostType type,
	boolean isDeleted,
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	LocalDateTime deletedAt,
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	LocalDateTime createdAt,
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	LocalDateTime modifiedAt
) {
	public static PostAdminResponse from(PostAdminInfo info) {
		return new PostAdminResponse(
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
