package nbc.chillguys.nebulazone.application.user.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.application.user.dto.request.TossPaymentConfirmRequest;
import nbc.chillguys.nebulazone.application.user.dto.response.PointChargeResponse;
import nbc.chillguys.nebulazone.application.user.service.PaymentService;
import nbc.chillguys.nebulazone.domain.auth.vo.AuthUser;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {

	private final PaymentService paymentService;

	@PostMapping("/confirm")
	public ResponseEntity<PointChargeResponse> chargePointWithToss(
		@AuthenticationPrincipal AuthUser authUser,
		@RequestBody TossPaymentConfirmRequest request) {

		PointChargeResponse response = paymentService.chargePointWithToss(authUser.getId(), request);
		return ResponseEntity.ok(response);
	}
}
