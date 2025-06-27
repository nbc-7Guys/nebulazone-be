package nbc.chillguys.nebulazone.domain.transaction.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import nbc.chillguys.nebulazone.domain.product.entity.Product;
import nbc.chillguys.nebulazone.domain.transaction.dto.TransactionAdminInfo;
import nbc.chillguys.nebulazone.domain.transaction.dto.TransactionAdminSearchQueryCommand;
import nbc.chillguys.nebulazone.domain.transaction.entity.Transaction;
import nbc.chillguys.nebulazone.domain.transaction.entity.TransactionMethod;
import nbc.chillguys.nebulazone.domain.transaction.entity.UserType;
import nbc.chillguys.nebulazone.domain.transaction.exception.TransactionException;
import nbc.chillguys.nebulazone.domain.transaction.repository.TransactionRepository;
import nbc.chillguys.nebulazone.domain.user.entity.User;

@ExtendWith(MockitoExtension.class)
class TransactionAdminDomainServiceTest {

	@Mock
	private TransactionRepository transactionRepository;

	@InjectMocks
	private TransactionAdminDomainService transactionAdminDomainService;

	@Test
	@DisplayName("findTransactions - Success")
	void successfindTransactions() {
		// Given
		TransactionAdminSearchQueryCommand command = new TransactionAdminSearchQueryCommand("keyword",
			TransactionMethod.DIRECT);
		Pageable pageable = PageRequest.of(0, 10);

		User mockUser = User.builder().nickname("testUser").build();
		ReflectionTestUtils.setField(mockUser, "id", 1L);

		Product mockProduct = Product.builder().name("testProduct").build();
		ReflectionTestUtils.setField(mockProduct, "id", 2L);

		Transaction mockTransaction = Transaction.builder()
			.price(1000L)
			.method(TransactionMethod.DIRECT)
			.user(mockUser)
			.userType(UserType.BUYER)
			.product(mockProduct)
			.createdAt(LocalDateTime.now())
			.build();
		ReflectionTestUtils.setField(mockTransaction, "id", 1L);

		Page<Transaction> mockTransactionPage = new PageImpl<>(Collections.singletonList(mockTransaction), pageable, 1);

		when(transactionRepository.searchTransactions(any(TransactionAdminSearchQueryCommand.class),
			any(Pageable.class)))
			.thenReturn(mockTransactionPage);

		// When
		Page<TransactionAdminInfo> result = transactionAdminDomainService.findTransactions(command, pageable);

		// Then
		assertEquals(1, result.getContent().size());
		assertEquals(mockTransaction.getId(), result.getContent().get(0).txId());
		verify(transactionRepository).searchTransactions(any(TransactionAdminSearchQueryCommand.class),
			any(Pageable.class));
	}

	@Test
	@DisplayName("deleteTransaction - Success")
	void successdeleteTransaction() {
		// Given
		Long txId = 1L;
		Transaction mockTransaction = Transaction.builder().build();
		ReflectionTestUtils.setField(mockTransaction, "id", txId);

		when(transactionRepository.findById(txId)).thenReturn(Optional.of(mockTransaction));
		doNothing().when(transactionRepository).delete(mockTransaction);

		// When
		transactionAdminDomainService.deleteTransaction(txId);

		// Then
		verify(transactionRepository).findById(txId);
		verify(transactionRepository).delete(mockTransaction);
	}

	@Test
	@DisplayName("deleteTransaction - Transaction Not Found")
	void notFound_deleteTransaction() {
		// Given
		Long txId = 1L;
		when(transactionRepository.findById(txId)).thenReturn(Optional.empty());

		// When & Then
		assertThrows(TransactionException.class, () -> transactionAdminDomainService.deleteTransaction(txId));
		verify(transactionRepository).findById(txId);
	}

	@Test
	@DisplayName("findByTransactionId - Success")
	void successfind_ByTransactionId() {
		// Given
		Long txId = 1L;
		Transaction mockTransaction = Transaction.builder().build();
		ReflectionTestUtils.setField(mockTransaction, "id", txId);

		when(transactionRepository.findById(txId)).thenReturn(Optional.of(mockTransaction));

		// When
		Transaction result = transactionAdminDomainService.findByTransactionId(txId);

		// Then
		assertEquals(txId, result.getId());
		verify(transactionRepository).findById(txId);
	}

	@Test
	@DisplayName("findByTransactionId - Transaction Not Found")
	void notFound_findByTransactionId() {
		// Given
		Long txId = 1L;
		when(transactionRepository.findById(txId)).thenReturn(Optional.empty());

		// When & Then
		assertThrows(TransactionException.class, () -> transactionAdminDomainService.findByTransactionId(txId));
		verify(transactionRepository).findById(txId);
	}
}
