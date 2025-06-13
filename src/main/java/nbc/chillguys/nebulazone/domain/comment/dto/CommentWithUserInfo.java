package nbc.chillguys.nebulazone.domain.comment.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import lombok.Getter;

@Getter
public class CommentWithUserInfo {
	private final Long commentId;
	private final String content;
	private final String author;
	private final Long parentId;
	private final LocalDateTime createdAt;
	private final LocalDateTime modifiedAt;
	private final List<CommentWithUserInfo> children;

	public CommentWithUserInfo(Long commentId, String content, String author, Long parentId, LocalDateTime createdAt, LocalDateTime modifiedAt) {
		this.commentId = commentId;
		this.content = content;
		this.author = author;
		this.parentId = parentId;
		this.createdAt = createdAt;
		this.modifiedAt = modifiedAt;
		this.children = new ArrayList<>();
	}
}
