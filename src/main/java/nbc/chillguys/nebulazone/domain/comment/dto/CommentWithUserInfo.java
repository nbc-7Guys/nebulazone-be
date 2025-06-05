package nbc.chillguys.nebulazone.domain.comment.dto;

import java.time.LocalDateTime;
import java.util.List;

public record CommentWithUserInfo(
	Long commentId,
	String content,
	String author,
	Long parentId,
	LocalDateTime createdAt,
	LocalDateTime modifiedAt,
	List<CommentWithUserInfo> children
) {
}
