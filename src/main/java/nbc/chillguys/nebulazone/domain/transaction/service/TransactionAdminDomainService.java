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

	/**
	 * 검색 조건과 페이징 정보에 따라 거래 내역을 조회합니다.
	 *
	 * @param command  거래 검색 조건
	 * @param pageable 페이징 정보
	 * @return 거래 정보 페이지
	 * @author 정석현
	 */
	@Transactional(readOnly = true)
	public Page<TransactionAdminInfo> findTransactions(TransactionAdminSearchQueryCommand command, Pageable pageable) {
		return transactionRepository.searchTransactions(command, pageable)
			.map(TransactionAdminInfo::from);
	}

	/**
	 * 거래 내역을 삭제합니다.
	 *
	 * @param txId 삭제할 거래의 ID
	 * @author 정석현
	 */
	@Transactional
	public void deleteTransaction(Long txId) {
		Transaction transaction = findByTransactionId(txId);
		transactionRepository.delete(transaction);
	}

	/**
	 * 거래 ID로 거래 내역을 조회합니다.<br>
	 * 존재하지 않으면 예외를 발생시킵니다.
	 *
	 * @param txId 거래 ID
	 * @return 거래 엔티티
	 * @author 정석현
	 */
	public Transaction findByTransactionId(Long txId) {
		return transactionRepository.findById(txId)
			.orElseThrow(() -> new TransactionException(TransactionErrorCode.NOT_FOUNT_TRANSACTION));
	}
}
