package nbc.chillguys.nebulazone.domain.catalog.vo;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Setting;

@Document(indexName = "catalogs")
@Setting(settingPath = "/elastic/catalog-settings.json")
public record CatalogDocument(
	@Id
	@Field(type = FieldType.Long)
	Long catalogId,

	@Field(type = FieldType.Text, analyzer = "korean_english")
	String name,

	@Field(type = FieldType.Text, analyzer = "korean_english")
	String description,

	@Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second)
	LocalDateTime createdAt,

	@Field(type = FieldType.Keyword)
	String type,

	@Field(type = FieldType.Keyword)
	String manufacturer,

	@Field(type = FieldType.Keyword)
	String chipset,

	@Field(type = FieldType.Keyword)
	String formFactor,

	@Field(type = FieldType.Keyword)
	String socket
) {
}
