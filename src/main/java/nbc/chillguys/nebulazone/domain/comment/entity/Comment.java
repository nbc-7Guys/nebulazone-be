package nbc.chillguys.nebulazone.domain.comment.entity;

import java.time.LocalDateTime;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import nbc.chillguys.nebulazone.domain.comment.exception.CommentErrorCode;
import nbc.chillguys.nebulazone.domain.comment.exception.CommentException;
import nbc.chillguys.nebulazone.domain.common.audit.BaseEntity;
import nbc.chillguys.nebulazone.domain.post.entity.Post;
import nbc.chillguys.nebulazone.domain.user.entity.User;

@Getter
@Entity
@Table(name = "comments")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Comment extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "comment_id")
	private Long id;

	private String content;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "post_id", nullable = false)
	private Post post;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "parent_id")
	private Comment parent;

	@Column(name = "is_deleted")
	private boolean deleted;

	private LocalDateTime deletedAt;

	@Builder
	public Comment(String content, Post post, User user, Comment parent) {
		this.content = content;
		this.post = post;
		this.user = user;
		this.parent = parent;
	}

	public void update(String content) {
		this.content = content;
	}

	public void validateBelongsToPost(Long postId) {
		if (!Objects.equals(getPost().getId(), postId)) {
			throw new CommentException(CommentErrorCode.NOT_BELONG_TO_POST);
		}
	}

	public void validateCommentOwner(Long userId) {
		if (!Objects.equals(getUser().getId(), userId)) {
			throw new CommentException(CommentErrorCode.NOT_COMMENT_OWNER);
		}
	}

	public void delete() {
		this.deleted = true;
		this.deletedAt = LocalDateTime.now();
	}

	public void restore() {
		this.deleted = false;
		this.deletedAt = null;
	}

}
