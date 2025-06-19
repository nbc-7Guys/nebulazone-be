package nbc.chillguys.nebulazone.application.user.dto.response;

public record TossPaymentConfirmResponse(
	String paymentKey,
	String orderId,
	String status
) {
}
