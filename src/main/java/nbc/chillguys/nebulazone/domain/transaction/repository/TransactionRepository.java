package nbc.chillguys.nebulazone.domain.transaction.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import nbc.chillguys.nebulazone.domain.transaction.entity.Transaction;

public interface TransactionRepository extends JpaRepository<Transaction, Long>, CustomTransactionAdminRepository {
}
