package nbc.chillguys.nebulazone.domain.product.event;

import java.time.LocalDateTime;

import nbc.chillguys.nebulazone.domain.product.entity.Product;
import nbc.chillguys.nebulazone.domain.user.entity.User;

public record PurchaseProductEvent(
	User buyer,
	Product product,
	LocalDateTime purchasedAt
) {
}
