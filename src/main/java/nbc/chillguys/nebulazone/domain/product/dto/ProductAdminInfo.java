package nbc.chillguys.nebulazone.domain.product.dto;

import java.time.LocalDateTime;
import java.util.List;

import nbc.chillguys.nebulazone.domain.product.entity.Product;
import nbc.chillguys.nebulazone.domain.product.entity.ProductImage;

public record ProductAdminInfo(
	Long productId,
	String name,
	String description,
	Long price,
	String txMethod,
	Boolean isSold,
	String sellerNickname,
	String catalogName,
	List<ProductImage> images,
	Boolean isDeleted,
	LocalDateTime deletedAt,
	LocalDateTime createdAt,
	LocalDateTime modifiedAt
) {
	public static ProductAdminInfo from(Product product) {
		return new ProductAdminInfo(
			product.getId(),
			product.getName(),
			product.getDescription(),
			product.getPrice(),
			product.getTxMethod().name(),
			product.isSold(),
			product.getSeller().getNickname(),
			product.getCatalog() != null ? product.getCatalog().getName() : null,
			product.getProductImages(),
			product.isDeleted(),
			product.getDeletedAt(),
			product.getCreatedAt(),
			product.getModifiedAt()
		);
	}
}
