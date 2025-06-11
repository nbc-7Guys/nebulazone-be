package nbc.chillguys.nebulazone.domain.transaction.repository;

import org.springframework.data.domain.Page;

import nbc.chillguys.nebulazone.domain.transaction.dto.TransactionFindAllInfo;
import nbc.chillguys.nebulazone.domain.user.entity.User;

public interface TransactionCustomRepository {
	Page<TransactionFindAllInfo> findTransactionsWithProduct(User user, int page, int size);
}
