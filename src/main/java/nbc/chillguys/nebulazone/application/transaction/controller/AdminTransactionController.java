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
import nbc.chillguys.nebulazone.application.transaction.dto.request.AdminTransactionSearchRequest;
import nbc.chillguys.nebulazone.application.transaction.dto.response.AdminTransactionResponse;
import nbc.chillguys.nebulazone.application.transaction.service.AdminTransactionService;
import nbc.chillguys.nebulazone.common.response.CommonPageResponse;
import nbc.chillguys.nebulazone.domain.transaction.entity.TransactionMethod;

@RestController
@RequestMapping("/admin/transactions")
@RequiredArgsConstructor
public class AdminTransactionController {
	private final AdminTransactionService adminTransactionService;

	@GetMapping
	public ResponseEntity<CommonPageResponse<AdminTransactionResponse>> findTransactions(
		@RequestParam(value = "keyword", required = false) String keyword,
		@RequestParam(value = "method", required = false) TransactionMethod method,
		@RequestParam(value = "page", defaultValue = "1") int page,
		@RequestParam(value = "size", defaultValue = "10") int size
	) {
		AdminTransactionSearchRequest request = new AdminTransactionSearchRequest(keyword, method, page, size);
		Pageable pageable = PageRequest.of(page - 1, size);
		CommonPageResponse<AdminTransactionResponse> response = adminTransactionService.findTransactions(request,
			pageable);
		return ResponseEntity.ok(response);
	}

	@DeleteMapping("/{txId}")
	public ResponseEntity<Void> deleteTransaction(@PathVariable Long txId) {
		adminTransactionService.deleteTransaction(txId);
		return ResponseEntity.noContent().build();
	}
}
