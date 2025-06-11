package nbc.chillguys.nebulazone.domain.transaction.service;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.domain.transaction.dto.TransactionCreateCommand;
import nbc.chillguys.nebulazone.domain.transaction.dto.TransactionFindAllInfo;
import nbc.chillguys.nebulazone.domain.transaction.dto.TransactionFindDetailInfo;
import nbc.chillguys.nebulazone.domain.transaction.entity.Transaction;
import nbc.chillguys.nebulazone.domain.transaction.entity.TransactionMethod;
import nbc.chillguys.nebulazone.domain.transaction.exception.TransactionErrorCode;
import nbc.chillguys.nebulazone.domain.transaction.exception.TransactionException;
import nbc.chillguys.nebulazone.domain.transaction.repository.TransactionRepository;
import nbc.chillguys.nebulazone.domain.user.entity.User;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class TransactionDomainService {

	private final TransactionRepository transactionRepository;

	/**
	 * 거래내역 저장
	 * @param command command
	 * @return 저장된 거래내역
	 * @author 윤정환
	 */
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

	/**
	 * 내 거래내역 전체 조회
	 * @param user 로그인 user
	 * @param page page
	 * @param size size
	 * @return 페이징 적용된 나의 거래내역 정보
	 * @author 전나겸
	 */
	public Page<TransactionFindAllInfo> findMyTransactions(User user, int page, int size) {
		return transactionRepository.findTransactionsWithProductAndUser(user, page, size);
	}

	/**
	 * 내 거래내역 상세 조회
	 * @param user 로그인 user
	 * @param transactionId 조회할 트랜잭션 아이디
	 * @return 조회된 거래내역 정보
	 * @author 전나겸
	 */
	public TransactionFindDetailInfo findMyTransaction(User user, Long transactionId) {
		return transactionRepository.findTransactionWithProductAndUser(user, transactionId)
			.orElseThrow(() -> new TransactionException(TransactionErrorCode.TRANSACTION_NOT_FOUND));
	}
}
