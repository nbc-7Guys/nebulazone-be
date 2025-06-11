package nbc.chillguys.nebulazone.domain.transaction.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;

import nbc.chillguys.nebulazone.domain.transaction.dto.TransactionFindAllInfo;
import nbc.chillguys.nebulazone.domain.transaction.dto.TransactionFindDetailInfo;
import nbc.chillguys.nebulazone.domain.user.entity.User;

public interface TransactionCustomRepository {
	Page<TransactionFindAllInfo> findTransactionsWithProductAndUser(User user, int page, int size);

	Optional<TransactionFindDetailInfo> findTransactionWithProductAndUser(User user, Long transactionId);
}
