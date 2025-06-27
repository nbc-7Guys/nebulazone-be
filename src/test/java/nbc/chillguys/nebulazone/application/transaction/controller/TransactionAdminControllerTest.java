package nbc.chillguys.nebulazone.application.transaction.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import nbc.chillguys.nebulazone.application.transaction.dto.response.TransactionAdminResponse;
import nbc.chillguys.nebulazone.application.transaction.service.TransactionAdminService;
import nbc.chillguys.nebulazone.common.response.CommonPageResponse;
import nbc.chillguys.nebulazone.config.TestSecurityConfig;
import nbc.chillguys.nebulazone.support.MockMvc.TestMockConfig;

@WebMvcTest(TransactionAdminController.class)
@Import({TestSecurityConfig.class, TestMockConfig.class})
class TransactionAdminControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private TransactionAdminService transactionAdminService;

	@Test
	@DisplayName("findTransactions - Success")
	void success_findTransactions() throws Exception {
		// Given
		TransactionAdminResponse mockResponse = new TransactionAdminResponse(
			1L,
			1000L,
			"DIRECT",
			"testUser",
			2L,
			"testProduct",
			LocalDateTime.now(),
			LocalDateTime.now()
		);
		List<TransactionAdminResponse> content = Collections.singletonList(mockResponse);
		CommonPageResponse<TransactionAdminResponse> commonPageResponse = CommonPageResponse.from(
			new PageImpl<>(content)
		);

		when(transactionAdminService.findTransactions(any(), any(Pageable.class)))
			.thenReturn(commonPageResponse);

		// When & Then
		mockMvc.perform(get("/admin/transactions")
				.param("keyword", "test")
				.param("method", "DIRECT")
				.param("page", "1")
				.param("size", "10"))
			.andExpect(status().isOk());
	}

	@Test
	@DisplayName("deleteTransaction - Success")
	void success_deleteTransaction() throws Exception {
		// Given
		Long txId = 1L;
		doNothing().when(transactionAdminService).deleteTransaction(txId);

		// When & Then
		mockMvc.perform(delete("/admin/transactions/{txId}", txId))
			.andExpect(status().isNoContent());
	}
}
