package nbc.chillguys.nebulazone.application.product.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonFormat;

import nbc.chillguys.nebulazone.domain.product.dto.ProductAdminInfo;
import nbc.chillguys.nebulazone.domain.product.entity.ProductImage;

public record ProductAdminResponse(
	Long productId,
	String name,
	String description,
	Long price,
	String txMethod,             // 거래방식명
	Boolean isSold,
	String sellerNickname,
	String catalogName,
	List<ImageResponse> images,
	Boolean isDeleted,
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	LocalDateTime deletedAt,
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	LocalDateTime createdAt,
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	LocalDateTime modifiedAt
) {
	public static ProductAdminResponse from(ProductAdminInfo info) {
		return new ProductAdminResponse(
			info.productId(),
			info.name(),
			info.description(),
			info.price(),
			info.txMethod(),
			info.isSold(),
			info.sellerNickname(),
			info.catalogName(),
			info.images().stream().map(ImageResponse::from).collect(Collectors.toList()),
			info.isDeleted(),
			info.deletedAt(),
			info.createdAt(),
			info.modifiedAt()
		);
	}

	public record ImageResponse(
		String url
	) {
		public static ImageResponse from(ProductImage img) {
			return new ImageResponse(img.getUrl());
		}
	}
}
