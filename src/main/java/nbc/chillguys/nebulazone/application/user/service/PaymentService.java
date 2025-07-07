package nbc.chillguys.nebulazone.application.user.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.application.user.dto.request.TossPaymentConfirmRequest;
import nbc.chillguys.nebulazone.application.user.dto.request.UserPointRequest;
import nbc.chillguys.nebulazone.application.user.dto.response.PointChargeResponse;
import nbc.chillguys.nebulazone.application.user.dto.response.TossPaymentConfirmResponse;
import nbc.chillguys.nebulazone.domain.pointhistory.dto.PointHistoryCommand;
import nbc.chillguys.nebulazone.domain.pointhistory.entity.PointHistoryStatus;
import nbc.chillguys.nebulazone.domain.pointhistory.entity.PointHistoryType;
import nbc.chillguys.nebulazone.domain.pointhistory.service.PointHistoryDomainService;
import nbc.chillguys.nebulazone.domain.user.entity.User;
import nbc.chillguys.nebulazone.infra.payment.PaymentClient;

@Service
@RequiredArgsConstructor
public class PaymentService {
	private final PaymentClient paymentClient;
	private final PointHistoryDomainService pointHistoryDomainService;
	private final PaymentTransactionService paymentTransactionService;

	@Transactional
	public void requestPointCharge(User user, UserPointRequest request) {

		PointHistoryCommand historyCommand = new PointHistoryCommand(user, request.price(), null,
			PointHistoryType.CHARGE);

		pointHistoryDomainService.createPointHistory(historyCommand, PointHistoryStatus.PENDING);
	}

	public PointChargeResponse chargePointWithToss(User user, TossPaymentConfirmRequest request) {
		TossPaymentConfirmResponse tossResult = paymentClient.confirmTossPayment(request);

		return paymentTransactionService.executePointChargeTransaction(user, request, tossResult);
	}

}
