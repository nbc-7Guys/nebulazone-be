package nbc.chillguys.nebulazone.domain.transaction.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.domain.transaction.dto.TransactionAdminInfo;
import nbc.chillguys.nebulazone.domain.transaction.dto.TransactionAdminSearchQueryCommand;
import nbc.chillguys.nebulazone.domain.transaction.entity.Transaction;
import nbc.chillguys.nebulazone.domain.transaction.exception.TransactionErrorCode;
import nbc.chillguys.nebulazone.domain.transaction.exception.TransactionException;
import nbc.chillguys.nebulazone.domain.transaction.repository.TransactionRepository;

@Service
@RequiredArgsConstructor
public class TransactionAdminDomainService {
	private final TransactionRepository transactionRepository;

	@Transactional(readOnly = true)
	public Page<TransactionAdminInfo> findTransactions(TransactionAdminSearchQueryCommand command, Pageable pageable) {
		return transactionRepository.searchTransactions(command, pageable)
			.map(TransactionAdminInfo::from);
	}

	@Transactional
	public void deleteTransaction(Long txId) {
		Transaction transaction = findByTransactionId(txId);
		transactionRepository.delete(transaction);
	}

	public Transaction findByTransactionId(Long txId) {
		return transactionRepository.findById(txId)
			.orElseThrow(() -> new TransactionException(TransactionErrorCode.NOT_FOUNT_TRANSACTION));
	}
}
