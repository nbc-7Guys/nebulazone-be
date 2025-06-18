package nbc.chillguys.nebulazone.application.user.dto.request;

import java.util.Map;

public record TossPaymentConfirmRequest(
	String paymentKey,
	String orderId,
	Long point
) {
	public Map<String, Object> toBody() {
		return Map.of(
			"paymentKey", paymentKey,
			"orderId", orderId,
			"amount", point
		);
	}
}
