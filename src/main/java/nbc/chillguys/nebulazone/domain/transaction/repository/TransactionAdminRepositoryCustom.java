package nbc.chillguys.nebulazone.domain.transaction.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import nbc.chillguys.nebulazone.domain.transaction.dto.TransactionAdminSearchQueryCommand;
import nbc.chillguys.nebulazone.domain.transaction.entity.Transaction;

public interface TransactionAdminRepositoryCustom {
	Page<Transaction> searchTransactions(TransactionAdminSearchQueryCommand command, Pageable pageable);
}
