package nbc.chillguys.nebulazone.application.pointhistory.controller;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import nbc.chillguys.nebulazone.application.pointhistory.dto.request.PointHistoryAdminRequest;
import nbc.chillguys.nebulazone.application.pointhistory.dto.response.AdminPointHistoryResponse;
import nbc.chillguys.nebulazone.application.pointhistory.service.PointHistoryAdminService;
import nbc.chillguys.nebulazone.common.response.CommonPageResponse;
import nbc.chillguys.nebulazone.config.TestSecurityConfig;
import nbc.chillguys.nebulazone.domain.pointhistory.entity.PointHistoryStatus;
import nbc.chillguys.nebulazone.domain.pointhistory.entity.PointHistoryType;
import nbc.chillguys.nebulazone.infra.security.filter.JwtAuthenticationFilter;
import nbc.chillguys.nebulazone.support.mock.TestMockConfig;
import nbc.chillguys.nebulazone.support.mockuser.WithCustomMockUser;

@DisplayName("포인트 히스토리 어드민 컨트롤러 테스트")
@Import({TestSecurityConfig.class, TestMockConfig.class})
@WebMvcTest(
	controllers = PointHistoryAdminController.class,
	excludeFilters = {
		@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {
			JwtAuthenticationFilter.class
		})
	}
)
class PointHistoryAdminControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private PointHistoryAdminService pointHistoryAdminService;

	@Nested
	@DisplayName("포인트 히스토리 목록 조회 테스트")
	@WithCustomMockUser
	class GetAdminPointHistoriesTest {

		@Test
		@DisplayName("성공: 포인트 히스토리 목록 조회")
		void success_getAdminPointHistories() throws Exception {
			// Given
			List<AdminPointHistoryResponse> list = List.of(
				new AdminPointHistoryResponse(
					1L, // pointId
					1000L, // price
					"123-456-789", // account
					PointHistoryType.CHARGE, // type
					PointHistoryStatus.PENDING, // status
					LocalDateTime.now(), // createdAt
					1L, // userId
					"user1@example.com", // email
					"User1" // nickname
				),
				new AdminPointHistoryResponse(
					2L, // pointId
					2000L, // price
					"987-654-321", // account
					PointHistoryType.EXCHANGE, // type
					PointHistoryStatus.ACCEPT, // status
					LocalDateTime.now(), // createdAt
					2L, // userId
					"user2@example.com", // email
					"User2" // nickname
				)
			);

			CommonPageResponse<AdminPointHistoryResponse> pageResponse =
				CommonPageResponse.from(new PageImpl<>(list));

			BDDMockito.given(
					pointHistoryAdminService.searchAdminPointHistories(any(PointHistoryAdminRequest.class),
						any(PageRequest.class)))
				.willReturn(pageResponse);

			// When & Then
			mockMvc.perform(get("/admin/points/histories")
					.param("page", "1")
					.param("size", "10"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content", hasSize(2)))
				.andExpect(jsonPath("$.content[0].pointId").value(1))
				.andExpect(jsonPath("$.content[0].email").value("user1@example.com"))
				.andExpect(jsonPath("$.content[0].nickname").value("User1"))
				.andExpect(jsonPath("$.content[0].price").value(1000))
				.andExpect(jsonPath("$.content[0].type").value("CHARGE"))
				.andExpect(jsonPath("$.content[0].status").value("PENDING"))
				.andExpect(jsonPath("$.content[1].pointId").value(2))
				.andExpect(jsonPath("$.content[1].email").value("user2@example.com"))
				.andExpect(jsonPath("$.content[1].nickname").value("User2"))
				.andExpect(jsonPath("$.content[1].price").value(2000))
				.andExpect(jsonPath("$.content[1].type").value("EXCHANGE"))
				.andExpect(jsonPath("$.content[1].status").value("ACCEPT"));
		}

		@Test
		@DisplayName("성공: 필터링 조건으로 포인트 히스토리 목록 조회")
		void success_getAdminPointHistories_withFilters() throws Exception {
			// Given
			List<AdminPointHistoryResponse> list = List.of(
				new AdminPointHistoryResponse(
					1L, // pointId
					1000L, // price
					"123-456-789", // account
					PointHistoryType.CHARGE, // type
					PointHistoryStatus.PENDING, // status
					LocalDateTime.now(), // createdAt
					1L, // userId
					"user1@example.com", // email
					"User1" // nickname
				)
			);

			CommonPageResponse<AdminPointHistoryResponse> pageResponse =
				CommonPageResponse.from(new PageImpl<>(list));

			BDDMockito.given(
					pointHistoryAdminService.searchAdminPointHistories(any(PointHistoryAdminRequest.class),
						any(PageRequest.class)))
				.willReturn(pageResponse);

			// When & Then
			mockMvc.perform(get("/admin/points/histories")
					.param("email", "user1@example.com")
					.param("nickname", "User1")
					.param("type", "CHARGE")
					.param("status", "PENDING")
					.param("page", "1")
					.param("size", "10"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content", hasSize(1)))
				.andExpect(jsonPath("$.content[0].email").value("user1@example.com"))
				.andExpect(jsonPath("$.content[0].nickname").value("User1"))
				.andExpect(jsonPath("$.content[0].type").value("CHARGE"))
				.andExpect(jsonPath("$.content[0].status").value("PENDING"));
		}
	}

	@Nested
	@DisplayName("포인트 요청 승인 테스트")
	@WithCustomMockUser
	class ApprovePointRequestTest {

		@Test
		@DisplayName("성공: 포인트 요청 승인")
		void success_approvePointRequest() throws Exception {
			// Given
			Long pointHistoryId = 1L;

			BDDMockito.willDoNothing()
				.given(pointHistoryAdminService)
				.approvePointHistory(pointHistoryId);

			// When & Then
			mockMvc.perform(post("/admin/points/points/{pointHistoryId}/approve", pointHistoryId))
				.andExpect(status().isOk())
				.andExpect(content().string(""));

			// Verify
			verify(pointHistoryAdminService, times(1))
				.approvePointHistory(pointHistoryId);
		}
	}

	@Nested
	@DisplayName("포인트 요청 거절 테스트")
	@WithCustomMockUser
	class RejectPointRequestTest {

		@Test
		@DisplayName("성공: 포인트 요청 거절")
		void success_rejectPointRequest() throws Exception {
			// Given
			Long pointHistoryId = 1L;

			BDDMockito.willDoNothing()
				.given(pointHistoryAdminService)
				.rejectPointHistory(pointHistoryId);

			// When & Then
			mockMvc.perform(post("/admin/points/points/{pointHistoryId}/reject", pointHistoryId))
				.andExpect(status().isOk())
				.andExpect(content().string(""));

			// Verify
			verify(pointHistoryAdminService, times(1))
				.rejectPointHistory(pointHistoryId);
		}
	}
}
