package nbc.chillguys.nebulazone.application.transaction.controller;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.application.transaction.dto.request.TransactionAdminSearchRequest;
import nbc.chillguys.nebulazone.application.transaction.dto.response.TransactionAdminResponse;
import nbc.chillguys.nebulazone.application.transaction.service.TransactionAdminService;
import nbc.chillguys.nebulazone.common.response.CommonPageResponse;
import nbc.chillguys.nebulazone.domain.transaction.entity.TransactionMethod;

@RestController
@RequestMapping("/admin/transactions")
@RequiredArgsConstructor
public class TransactionAdminController {
	private final TransactionAdminService transactionAdminService;

	@GetMapping
	public ResponseEntity<CommonPageResponse<TransactionAdminResponse>> findTransactions(
		@RequestParam(value = "keyword", required = false) String keyword,
		@RequestParam(value = "method", required = false) TransactionMethod method,
		@RequestParam(value = "page", defaultValue = "1") int page,
		@RequestParam(value = "size", defaultValue = "10") int size
	) {
		TransactionAdminSearchRequest request = new TransactionAdminSearchRequest(keyword, method, page, size);
		Pageable pageable = PageRequest.of(page - 1, size);
		CommonPageResponse<TransactionAdminResponse> response = transactionAdminService.findTransactions(request,
			pageable);
		return ResponseEntity.ok(response);
	}

	@DeleteMapping("/{txId}")
	public ResponseEntity<Void> deleteTransaction(@PathVariable Long txId) {
		transactionAdminService.deleteTransaction(txId);
		return ResponseEntity.noContent().build();
	}
}
