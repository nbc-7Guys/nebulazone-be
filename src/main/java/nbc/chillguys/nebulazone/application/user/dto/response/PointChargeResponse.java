package nbc.chillguys.nebulazone.application.user.dto.response;

import nbc.chillguys.nebulazone.domain.user.entity.User;

public record PointChargeResponse(
	Long userId,
	Long totalPoint,
	String paymentKey,
	String orderId,
	String status
) {
	public static PointChargeResponse from(User user, TossPaymentConfirmResponse payment) {
		return new PointChargeResponse(
			user.getId(),
			user.getPoint(),
			payment.paymentKey(),
			payment.orderId(),
			payment.status()
		);
	}
}
