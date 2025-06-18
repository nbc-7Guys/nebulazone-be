package nbc.chillguys.nebulazone.application.transaction.service;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.application.transaction.dto.response.FindDetailTransactionResponse;
import nbc.chillguys.nebulazone.application.transaction.dto.response.FindTransactionResponse;
import nbc.chillguys.nebulazone.common.response.CommonPageResponse;
import nbc.chillguys.nebulazone.domain.transaction.dto.TransactionFindAllInfo;
import nbc.chillguys.nebulazone.domain.transaction.dto.TransactionFindDetailInfo;
import nbc.chillguys.nebulazone.domain.transaction.service.TransactionDomainService;
import nbc.chillguys.nebulazone.domain.user.entity.User;

@Service
@RequiredArgsConstructor
public class TransactionService {

	private final TransactionDomainService txDomainService;

	public CommonPageResponse<FindTransactionResponse> findMyTransactions(User user, int page, int size) {

		Page<TransactionFindAllInfo> findTransaction = txDomainService.findMyTransactions(user, page, size);
		Page<FindTransactionResponse> response = findTransaction.map(FindTransactionResponse::from);
		return CommonPageResponse.from(response);
	}

	public FindDetailTransactionResponse findMyTransaction(User user, Long transactionId) {

		TransactionFindDetailInfo detailInfo = txDomainService.findMyTransaction(user, transactionId);

		return FindDetailTransactionResponse.from(detailInfo);
	}
}
