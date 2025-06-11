package nbc.chillguys.nebulazone.application.transaction.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.application.transaction.dto.request.AdminTransactionSearchRequest;
import nbc.chillguys.nebulazone.application.transaction.dto.response.AdminTransactionResponse;
import nbc.chillguys.nebulazone.common.response.CommonPageResponse;
import nbc.chillguys.nebulazone.domain.transaction.dto.AdminTransactionInfo;
import nbc.chillguys.nebulazone.domain.transaction.dto.AdminTransactionSearchQueryCommand;
import nbc.chillguys.nebulazone.domain.transaction.service.AdminTransactionDomainService;

@Service
@RequiredArgsConstructor
public class AdminTransactionService {
	private final AdminTransactionDomainService adminTransactionDomainService;

	public CommonPageResponse<AdminTransactionResponse> findTransactions(AdminTransactionSearchRequest request,
		Pageable pageable) {
		AdminTransactionSearchQueryCommand command = new AdminTransactionSearchQueryCommand(
			request.keyword(),
			request.method()
		);
		Page<AdminTransactionInfo> infoPage = adminTransactionDomainService.findTransactions(command, pageable);
		return CommonPageResponse.from(infoPage.map(AdminTransactionResponse::from));
	}

	public void deleteTransaction(Long txId) {
		adminTransactionDomainService.deleteTransaction(txId);
	}
}
