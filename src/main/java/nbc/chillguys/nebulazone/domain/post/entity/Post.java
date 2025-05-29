package nbc.chillguys.nebulazone.domain.post.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import nbc.chillguys.nebulazone.domain.common.audit.BaseEntity;
import nbc.chillguys.nebulazone.domain.user.entity.User;

@Getter
@Entity
@Table(name = "posts")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Post extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "post_id")
	private Long id;

	@Column(nullable = false)
	private String title;

	@Lob
	@Column(nullable = false)
	private String content;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private PostType type;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	private boolean isDeleted;

	private LocalDateTime deletedAt;

	@ElementCollection
	@CollectionTable(name = "post_images", joinColumns = @JoinColumn(name = "post_id"))
	@Column(name = "image_url")
	private List<PostImage> postImages = new ArrayList<>();


	@Builder
	public Post(String title, String content, PostType type, User user) {
		this.title = title;
		this.content = content;
		this.type = type;
		this.user = user;
	}
}
