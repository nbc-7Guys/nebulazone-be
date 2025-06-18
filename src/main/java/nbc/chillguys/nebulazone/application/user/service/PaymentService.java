package nbc.chillguys.nebulazone.application.user.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.application.user.dto.request.TossPaymentConfirmRequest;
import nbc.chillguys.nebulazone.application.user.dto.response.PointChargeResponse;
import nbc.chillguys.nebulazone.application.user.dto.response.TossPaymentConfirmResponse;
import nbc.chillguys.nebulazone.domain.pointhistory.dto.PointHistoryCommand;
import nbc.chillguys.nebulazone.domain.pointhistory.entity.PointHistoryType;
import nbc.chillguys.nebulazone.domain.pointhistory.service.PointHistoryDomainService;
import nbc.chillguys.nebulazone.domain.user.dto.UserPointChargeCommand;
import nbc.chillguys.nebulazone.domain.user.entity.User;
import nbc.chillguys.nebulazone.domain.user.service.UserDomainService;
import nbc.chillguys.nebulazone.infra.payment.PaymentClient;

@Service
@RequiredArgsConstructor
public class PaymentService {
	private final PaymentClient paymentClient;
	private final UserDomainService userDomainService;
	private final PointHistoryDomainService pointHistoryDomainService;

	@Transactional
	public PointChargeResponse chargePointWithToss(Long userId, TossPaymentConfirmRequest request) {
		TossPaymentConfirmResponse tossResult = paymentClient.confirmTossPayment(request);

		UserPointChargeCommand command = new UserPointChargeCommand(userId, request.point());
		User user = userDomainService.findActiveUserById(command.userId());
		userDomainService.chargeUserPoint(command);

		PointHistoryCommand historyCommand = new PointHistoryCommand(user, request.point(), null,
			PointHistoryType.CHARGE);
		pointHistoryDomainService.createPointHistory(historyCommand);
		return PointChargeResponse.from(user, tossResult);
	}
}
