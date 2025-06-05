package nbc.chillguys.nebulazone.domain.transaction.service;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.domain.transaction.dto.TransactionCreateCommand;
import nbc.chillguys.nebulazone.domain.transaction.entity.Transaction;
import nbc.chillguys.nebulazone.domain.transaction.entity.TransactionMethod;
import nbc.chillguys.nebulazone.domain.transaction.repository.TransactionRepository;

@RequiredArgsConstructor
@Service
public class TransactionDomainService {

	private final TransactionRepository transactionRepository;

	public Transaction createTransaction(TransactionCreateCommand command) {
		Transaction tx = Transaction.builder()
			.product(command.product())
			.user(command.user())
			.method(TransactionMethod.of(command.txMethod()))
			.price(command.product().getPrice())
			.build();
		return transactionRepository.save(tx);
	}
}
