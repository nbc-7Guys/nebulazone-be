package nbc.chillguys.nebulazone.domain.post.vo;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Setting;

import nbc.chillguys.nebulazone.domain.post.entity.Post;
import nbc.chillguys.nebulazone.domain.post.entity.PostImage;

@Document(indexName = "posts")
@Setting(settingPath = "/elastic/settings.json")
public record PostDocument(
	@Id
	@Field(type = FieldType.Long)
	Long postId,

	@Field(type = FieldType.Text, analyzer = "korean_english")
	String title,

	@Field(type = FieldType.Text, analyzer = "korean_english")
	String content,

	@Field(type = FieldType.Keyword)
	String type,

	@Field(type = FieldType.Long)
	Long userId,

	@Field(type = FieldType.Keyword)
	String author,

	@Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second)
	LocalDateTime createdAt,

	@Field(type = FieldType.Keyword)
	List<String> imageUrls
) {
	public static PostDocument from(Post post) {
		return new PostDocument(
			post.getId(),
			post.getTitle(),
			post.getContent(),
			post.getType().name(),
			post.getUserId(),
			post.getUserNickname(),
			post.getCreatedAt(),
			post.getPostImages().stream()
				.map(PostImage::getUrl)
				.toList()
		);
	}
}
