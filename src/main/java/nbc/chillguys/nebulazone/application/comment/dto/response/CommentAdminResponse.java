package nbc.chillguys.nebulazone.application.comment.dto.response;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import nbc.chillguys.nebulazone.domain.comment.dto.CommentAdminInfo;

public record CommentAdminResponse(
	Long commentId,
	String content,
	String writer,
	Long postId,
	String postTitle,
	Long parentId,                // 대댓글일 경우
	Boolean deleted,
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	LocalDateTime deletedAt,
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	LocalDateTime createdAt,
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	LocalDateTime modifiedAt
) {
	public static CommentAdminResponse from(CommentAdminInfo info) {
		return new CommentAdminResponse(
			info.commentId(),
			info.content(),
			info.writer(),
			info.postId(),
			info.postTitle(),
			info.parentId(),
			info.deleted(),
			info.deletedAt(),
			info.createdAt(),
			info.modifiedAt()
		);
	}
}
