package nbc.chillguys.nebulazone.application.comment.dto.response;

import java.time.LocalDateTime;

import nbc.chillguys.nebulazone.domain.comment.dto.AdminCommentInfo;

public record AdminCommentResponse(
	Long commentId,
	String content,
	String writer,
	Long postId,
	String postTitle,
	Long parentId,                // 대댓글일 경우
	Boolean deleted,
	LocalDateTime deletedAt,
	LocalDateTime createdAt,
	LocalDateTime modifiedAt
) {
	public static AdminCommentResponse from(AdminCommentInfo info) {
		return new AdminCommentResponse(
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
