package nbc.chillguys.nebulazone.domain.transaction.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import nbc.chillguys.nebulazone.domain.transaction.dto.AdminTransactionSearchQueryCommand;
import nbc.chillguys.nebulazone.domain.transaction.entity.Transaction;

public interface CustomTransactionAdminRepository {
	public Page<Transaction> searchTransactions(AdminTransactionSearchQueryCommand command, Pageable pageable);
}
