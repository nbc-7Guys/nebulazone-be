package nbc.chillguys.nebulazone.application.transaction.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Collections;

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

import nbc.chillguys.nebulazone.application.transaction.dto.request.TransactionAdminSearchRequest;
import nbc.chillguys.nebulazone.application.transaction.dto.response.TransactionAdminResponse;
import nbc.chillguys.nebulazone.common.response.CommonPageResponse;
import nbc.chillguys.nebulazone.domain.transaction.dto.TransactionAdminInfo;
import nbc.chillguys.nebulazone.domain.transaction.dto.TransactionAdminSearchQueryCommand;
import nbc.chillguys.nebulazone.domain.transaction.entity.TransactionMethod;
import nbc.chillguys.nebulazone.domain.transaction.service.TransactionAdminDomainService;

@ExtendWith(MockitoExtension.class)
class TransactionAdminServiceTest {

	@Mock
	private TransactionAdminDomainService transactionAdminDomainService;

	@InjectMocks
	private TransactionAdminService transactionAdminService;

	@Test
	@DisplayName("findTransactions - Success")
	void success_findTransactions() {
		// Given
		TransactionAdminSearchRequest request = new TransactionAdminSearchRequest("keyword", TransactionMethod.DIRECT,
			1, 10);
		Pageable pageable = PageRequest.of(0, 10);

		TransactionAdminInfo mockInfo = new TransactionAdminInfo(
			1L,
			1000L,
			TransactionMethod.DIRECT.name(),
			"testUser", // userNickname
			2L,
			"testProduct",
			LocalDateTime.now(),
			LocalDateTime.now()
		);
		Page<TransactionAdminInfo> mockInfoPage = new PageImpl<>(Collections.singletonList(mockInfo), pageable, 1);

		when(transactionAdminDomainService.findTransactions(any(TransactionAdminSearchQueryCommand.class),
			any(Pageable.class)))
			.thenReturn(mockInfoPage);

		// When
		CommonPageResponse<TransactionAdminResponse> result = transactionAdminService.findTransactions(request,
			pageable);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.content()).hasSize(1);
		assertThat(result.totalElements()).isEqualTo(1);
		assertThat(result.page()).isEqualTo(1);
		assertThat(result.size()).isEqualTo(10);
		assertThat(result.totalPages()).isEqualTo(1);
		assertThat(result.hasNext()).isFalse();
		assertThat(result.isLast()).isTrue();
		assertThat(result.hasPrevious()).isFalse();
		assertThat(result.isFirst()).isTrue();
		TransactionAdminResponse actualResponse = result.content().get(0);
		assertThat(actualResponse.txId()).isEqualTo(mockInfo.txId());
		assertThat(actualResponse.price()).isEqualTo(mockInfo.price());
		assertThat(actualResponse.method()).isEqualTo(mockInfo.method());
		assertThat(actualResponse.userNickname()).isEqualTo(mockInfo.userNickname());
		assertThat(actualResponse.productId()).isEqualTo(mockInfo.productId());
		assertThat(actualResponse.productName()).isEqualTo(mockInfo.productName());
		assertThat(actualResponse.createdAt()).isEqualTo(mockInfo.createdAt());
		assertThat(actualResponse.modifiedAt()).isEqualTo(mockInfo.modifiedAt());

		verify(transactionAdminDomainService).findTransactions(any(TransactionAdminSearchQueryCommand.class),
			any(Pageable.class));
	}

	@Test
	@DisplayName("deleteTransaction - Success")
	void success_deleteTransaction() {
		// Given
		Long txId = 1L;
		doNothing().when(transactionAdminDomainService).deleteTransaction(txId);

		// When
		transactionAdminService.deleteTransaction(txId);

		// Then
		verify(transactionAdminDomainService).deleteTransaction(txId);
	}
}
