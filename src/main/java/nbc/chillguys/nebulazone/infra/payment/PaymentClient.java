package nbc.chillguys.nebulazone.infra.payment;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.application.user.dto.request.TossPaymentConfirmRequest;
import nbc.chillguys.nebulazone.application.user.dto.response.TossPaymentConfirmResponse;

@Service
@RequiredArgsConstructor
public class PaymentClient {

	private final RestClient restClient;

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

		Map<String, Object> body = Map.of(
			"paymentKey", request.paymentKey(),
			"orderId", request.orderId(),
			"amount", request.point()
		);

		return restClient.post()
			.uri(url)
			.headers(headers -> {
				headers.set("Authorization", "Basic " + encodedSecretKey);
				headers.setContentType(MediaType.APPLICATION_JSON);
			})
			.body(body)
			.retrieve()
			.body(TossPaymentConfirmResponse.class);
	}
}
