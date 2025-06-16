package nbc.chillguys.nebulazone.domain.product.vo;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Setting;

import nbc.chillguys.nebulazone.domain.product.entity.Product;
import nbc.chillguys.nebulazone.domain.product.entity.ProductImage;

@Document(indexName = "products")
@Setting(settingPath = "/elastic/settings.json")
public record ProductDocument(
	@Id
	@Field(type = FieldType.Long)
	Long productId,

	@Field(type = FieldType.Text, analyzer = "korean_english")
	String productName,

	@Field(type = FieldType.Long)
	Long price,

	@Field(type = FieldType.Keyword)
	String txMethod,

	@Field(type = FieldType.Long)
	Long catalogId,

	@Field(type = FieldType.Long)
	Long sellerId,

	@Field(type = FieldType.Keyword)
	String sellerNickname,

	@Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second)
	LocalDateTime createdAt,

	@Field(type = FieldType.Keyword)
	List<String> imageUrls
) {
	public static ProductDocument from(Product product) {
		return new ProductDocument(
			product.getId(),
			product.getName(),
			product.getPrice(),
			product.getTxMethod().name(),
			product.getCatalogId(),
			product.getSellerId(),
			product.getSellerNickname(),
			product.getCreatedAt(),
			product.getProductImages().stream()
				.map(ProductImage::getUrl)
				.toList()
		);
	}
}
