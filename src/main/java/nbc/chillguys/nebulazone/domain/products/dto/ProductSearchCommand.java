package nbc.chillguys.nebulazone.domain.products.dto;

public record ProductSearchCommand(
	String productName,
	String txMethod,
	Long priceFrom,
	Long priceTo,
	int page,
	int size
) {
}
