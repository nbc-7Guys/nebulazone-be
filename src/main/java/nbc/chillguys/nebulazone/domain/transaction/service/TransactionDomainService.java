package nbc.chillguys.nebulazone.domain.transaction.service;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.domain.transaction.dto.TransactionCreateCommand;
import nbc.chillguys.nebulazone.domain.transaction.dto.TransactionFindAllInfo;
import nbc.chillguys.nebulazone.domain.transaction.entity.Transaction;
import nbc.chillguys.nebulazone.domain.transaction.entity.TransactionMethod;
import nbc.chillguys.nebulazone.domain.transaction.repository.TransactionRepository;
import nbc.chillguys.nebulazone.domain.user.entity.User;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class TransactionDomainService {

	private final TransactionRepository transactionRepository;

	@Transactional
	public Transaction createTransaction(TransactionCreateCommand command) {
		Transaction tx = Transaction.builder()
			.product(command.product())
			.user(command.user())
			.method(TransactionMethod.of(command.txMethod()))
			.price(command.price())
			.build();
		return transactionRepository.save(tx);
	}

	public Page<TransactionFindAllInfo> findTransactions(User user, int page, int size) {
		return transactionRepository.findTransactionsWithProduct(user, page, size);
	}
}
