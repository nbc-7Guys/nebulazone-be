package nbc.chillguys.nebulazone.infra.payment;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.application.user.dto.request.TossPaymentConfirmRequest;
import nbc.chillguys.nebulazone.application.user.dto.response.TossPaymentConfirmResponse;

@Service
@RequiredArgsConstructor
public class PaymentClient {

	private final RestTemplate restTemplate = new RestTemplate();

	@Value("${toss.secret-key}")
	private String secretKey;

	/**
	 * 결제 승인 처리 (토스 결제 승인 API 호출)
	 *
	 * @param request 결제 승인 요청 DTO
	 * @return 결제 승인 결과 DTO
	 */
	public TossPaymentConfirmResponse confirmTossPayment(TossPaymentConfirmRequest request) {
		String url = "https://api.tosspayments.com/v1/payments/confirm";
		String encodedSecretKey = Base64.getEncoder()
			.encodeToString((secretKey + ":").getBytes(StandardCharsets.UTF_8));

		HttpHeaders headers = new HttpHeaders();
		headers.set("Authorization", "Basic " + encodedSecretKey);
		headers.setContentType(MediaType.APPLICATION_JSON);

		Map<String, Object> body = Map.of(
			"paymentKey", request.paymentKey(),
			"orderId", request.orderId(),
			"point", request.point()
		);

		HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

		ResponseEntity<TossPaymentConfirmResponse> response = restTemplate
			.postForEntity(url, entity, TossPaymentConfirmResponse.class);

		if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
			return response.getBody();
		} else {
			throw new IllegalStateException("토스 결제 승인 실패");
		}
	}
}
