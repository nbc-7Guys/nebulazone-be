package nbc.chillguys.nebulazone.application.user.service;

import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.application.user.dto.request.TossPaymentConfirmRequest;
import nbc.chillguys.nebulazone.application.user.dto.response.PointChargeResponse;
import nbc.chillguys.nebulazone.application.user.dto.response.TossPaymentConfirmResponse;
import nbc.chillguys.nebulazone.domain.pointhistory.dto.PointHistoryCommand;
import nbc.chillguys.nebulazone.domain.pointhistory.entity.PointHistoryStatus;
import nbc.chillguys.nebulazone.domain.pointhistory.entity.PointHistoryType;
import nbc.chillguys.nebulazone.domain.pointhistory.service.PointHistoryDomainService;
import nbc.chillguys.nebulazone.domain.user.dto.UserPointChargeCommand;
import nbc.chillguys.nebulazone.domain.user.entity.User;
import nbc.chillguys.nebulazone.domain.user.service.UserDomainService;
import nbc.chillguys.nebulazone.infra.redis.service.UserCacheService;

@Service
@RequiredArgsConstructor
public class PaymentTransactionService {

	private final UserDomainService userDomainService;
	private final PointHistoryDomainService pointHistoryDomainService;
	private final UserCacheService userCacheService;

	@Transactional
	public PointChargeResponse executePointChargeTransaction(User user, TossPaymentConfirmRequest request,
		TossPaymentConfirmResponse tossResult) {
		UserPointChargeCommand command = new UserPointChargeCommand(user.getId(), request.point());
		userDomainService.chargeUserPoint(command);

		PointHistoryCommand historyCommand = new PointHistoryCommand(user, request.point(), null,
			PointHistoryType.CHARGE);
		pointHistoryDomainService.createPointHistory(historyCommand, PointHistoryStatus.ACCEPT);

		userCacheService.deleteUserById(user.getId());

		return PointChargeResponse.from(user, tossResult);
	}
}
