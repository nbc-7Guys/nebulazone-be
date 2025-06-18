package nbc.chillguys.nebulazone.application.user.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.application.user.dto.request.TossPaymentConfirmRequest;
import nbc.chillguys.nebulazone.application.user.dto.request.UserPointRequest;
import nbc.chillguys.nebulazone.application.user.dto.response.PointChargeResponse;
import nbc.chillguys.nebulazone.application.user.service.PaymentService;
import nbc.chillguys.nebulazone.domain.user.entity.User;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {

	private final PaymentService paymentService;

	@PostMapping("/confirm/toss")
	public ResponseEntity<PointChargeResponse> chargePointWithToss(
		@AuthenticationPrincipal User user,
		@RequestBody TossPaymentConfirmRequest request) {

		PointChargeResponse response = paymentService.chargePointWithToss(user, request);
		return ResponseEntity.ok(response);
	}

	@PostMapping("/charge/request")
	public ResponseEntity<Void> requestCharge(
		@AuthenticationPrincipal User user,
		@RequestBody UserPointRequest request
	) {
		paymentService.requestPointCharge(user, request);
		return ResponseEntity.ok().build();
	}
}
