package nbc.chillguys.nebulazone.domain.product.event;

import nbc.chillguys.nebulazone.domain.product.entity.Product;

public record ProductUpdatedEvent(
	Product product
) {
}
