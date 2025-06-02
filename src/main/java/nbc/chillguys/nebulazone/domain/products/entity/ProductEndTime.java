package nbc.chillguys.nebulazone.domain.products.entity;

import java.util.Arrays;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.domain.products.exception.ProductErrorCode;
import nbc.chillguys.nebulazone.domain.products.exception.ProductException;

@Getter
@RequiredArgsConstructor
public enum ProductEndTime {
	HOUR_12(12 * 60 * 60 * 1000),
	HOUR_24(24 * 60 * 60 * 1000),
	DAY_3(72 * 60 * 60 * 1000);

	private final long millis;

	public static ProductEndTime of(String endTime) {
		if (endTime == null || endTime.isBlank()) {
			return null;
		}

		return Arrays.stream(ProductEndTime.values())
			.filter(t -> t.name().equalsIgnoreCase(endTime))
			.findFirst()
			.orElseThrow(() -> new ProductException(ProductErrorCode.INVALID_END_TIME));
	}

}
