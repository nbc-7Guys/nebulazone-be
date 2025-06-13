package nbc.chillguys.nebulazone.application.transaction.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.application.transaction.dto.request.TransactionAdminSearchRequest;
import nbc.chillguys.nebulazone.application.transaction.dto.response.TransactionAdminResponse;
import nbc.chillguys.nebulazone.common.response.CommonPageResponse;
import nbc.chillguys.nebulazone.domain.transaction.dto.TransactionAdminInfo;
import nbc.chillguys.nebulazone.domain.transaction.dto.TransactionAdminSearchQueryCommand;
import nbc.chillguys.nebulazone.domain.transaction.service.TransactionAdminDomainService;

@Service
@RequiredArgsConstructor
public class TransactionAdminService {
	private final TransactionAdminDomainService transactionAdminDomainService;

	public CommonPageResponse<TransactionAdminResponse> findTransactions(TransactionAdminSearchRequest request,
		Pageable pageable) {
		TransactionAdminSearchQueryCommand command = new TransactionAdminSearchQueryCommand(
			request.keyword(),
			request.method()
		);
		Page<TransactionAdminInfo> infoPage = transactionAdminDomainService.findTransactions(command, pageable);
		return CommonPageResponse.from(infoPage.map(TransactionAdminResponse::from));
	}

	public void deleteTransaction(Long txId) {
		transactionAdminDomainService.deleteTransaction(txId);
	}
}
