package nbc.chillguys.nebulazone.application.product.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import nbc.chillguys.nebulazone.domain.products.dto.AdminProductInfo;
import nbc.chillguys.nebulazone.domain.products.entity.ProductImage;

public record AdminProductResponse(
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
	LocalDateTime deletedAt,
	LocalDateTime createdAt,
	LocalDateTime modifiedAt
) {
	public static AdminProductResponse from(AdminProductInfo info) {
		return new AdminProductResponse(
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
