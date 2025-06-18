package nbc.chillguys.nebulazone.application.user.dto.request;

public record TossPaymentConfirmRequest(
	String paymentKey,
	String orderId,
	Long point
) {
}
