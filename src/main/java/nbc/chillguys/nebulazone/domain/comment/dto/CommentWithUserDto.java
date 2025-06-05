package nbc.chillguys.nebulazone.domain.comment.dto;

import java.time.LocalDateTime;
import java.util.List;

public record CommentWithUserDto(
	Long commentId,
	String content,
	String author,
	Long parentId,
	LocalDateTime createdAt,
	LocalDateTime modifiedAt,
	List<CommentWithUserDto> children
) {
}
