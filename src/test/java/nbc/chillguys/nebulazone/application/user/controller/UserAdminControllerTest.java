package nbc.chillguys.nebulazone.application.user.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import nbc.chillguys.nebulazone.application.user.dto.request.UserAdminUpdateRequest;
import nbc.chillguys.nebulazone.application.user.dto.request.UserAdminUpdateRolesRequest;
import nbc.chillguys.nebulazone.application.user.dto.request.UserAdminUpdateStatusRequest;
import nbc.chillguys.nebulazone.application.user.dto.response.UserAdminResponse;
import nbc.chillguys.nebulazone.application.user.dto.response.UserResponse;
import nbc.chillguys.nebulazone.application.user.service.UserAdminService;
import nbc.chillguys.nebulazone.common.response.CommonPageResponse;
import nbc.chillguys.nebulazone.config.TestSecurityConfig;
import nbc.chillguys.nebulazone.domain.user.entity.OAuthType;
import nbc.chillguys.nebulazone.domain.user.entity.UserRole;
import nbc.chillguys.nebulazone.domain.user.entity.UserStatus;
import nbc.chillguys.nebulazone.support.MockMvc.TestMockConfig;

@DisplayName("유저 관리자 컨트롤러 단위 테스트")
@Import({TestSecurityConfig.class, TestMockConfig.class})
@WebMvcTest(UserAdminController.class)
class UserAdminControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private UserAdminService userAdminService;

	@Nested
	@DisplayName("관리자 유저 목록 조회 API 테스트")
	class FindUsersApiTest {

		@Test
		@WithMockUser(roles = "ADMIN")
		@DisplayName("성공")
		void success_findUsers() throws Exception {
			// given
			Pageable pageable = PageRequest.of(0, 10);
			UserAdminResponse userAdminResponse = new UserAdminResponse(
				1L, "test@test.com", "010-1234-5678", "testuser", 1000L,
				OAuthType.DOMAIN, null, Set.of(), Set.of(UserRole.ROLE_USER),
				UserStatus.ACTIVE, LocalDateTime.now(), LocalDateTime.now()
			);
			CommonPageResponse<UserAdminResponse> serviceResponse = CommonPageResponse.from(
				new PageImpl<>(List.of(userAdminResponse), pageable, 1)
			);

			given(userAdminService.findUsers(any(), any(Pageable.class))).willReturn(serviceResponse);

			// when & then
			mockMvc.perform(get("/admin/users")
					.param("page", "1")
					.param("size", "10")
					.param("status", "ACTIVE"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content").isArray())
				.andExpect(jsonPath("$.content[0].userId").value(1L))
				.andExpect(jsonPath("$.content[0].email").value("test@test.com"))
				.andExpect(jsonPath("$.content[0].nickname").value("testuser"))
				.andExpect(jsonPath("$.content[0].userStatus").value("ACTIVE"))
				.andExpect(jsonPath("$.page").value(1))
				.andDo(print());

			then(userAdminService).should(times(1)).findUsers(any(), any(Pageable.class));
		}
	}

	@Nested
	@DisplayName("유저 상세 조회 API 테스트")
	class GetUserDetailApiTest {

		@Test
		@WithMockUser(roles = "ADMIN")
		@DisplayName("성공")
		void success_getUserDetail() throws Exception {
			// given
			long userId = 1L;
			UserResponse serviceResponse = new UserResponse(
				userId, "test@test.com", "010-1234-5678", "testuser",
				"profile.jpg", 1000L, OAuthType.DOMAIN, null, Set.of(),
				LocalDateTime.now(), LocalDateTime.now()
			);
			given(userAdminService.getUserDetail(userId)).willReturn(serviceResponse);

			// when & then
			mockMvc.perform(get("/admin/users/{userId}", userId))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.userId").value(userId))
				.andExpect(jsonPath("$.email").value("test@test.com"))
				.andExpect(jsonPath("$.nickname").value("testuser"))
				.andDo(print());

			then(userAdminService).should(times(1)).getUserDetail(userId);
		}
	}

	@Nested
	@DisplayName("유저 정보 변경 API 테스트")
	class UpdateUserApiTest {

		@Test
		@WithMockUser(roles = "ADMIN")
		@DisplayName("성공")
		void success_updateUser() throws Exception {
			// given
			long userId = 1L;
			UserAdminUpdateRequest request = new UserAdminUpdateRequest("new@email.com", "010-8765-4321", "newNickname",
				"new.jpg");
			String requestJson = objectMapper.writeValueAsString(request);

			// when & then
			mockMvc.perform(patch("/admin/users/{userId}", userId)
					.contentType(MediaType.APPLICATION_JSON)
					.content(requestJson)
					.with(csrf()))
				.andExpect(status().isOk())
				.andDo(print());

			then(userAdminService).should(times(1)).updateUser(eq(userId), any(UserAdminUpdateRequest.class));
		}
	}

	@Nested
	@DisplayName("유저 상태 변경 API 테스트")
	class UpdateUserStatusApiTest {

		@Test
		@WithMockUser(roles = "ADMIN")
		@DisplayName("성공")
		void success_updateUserStatus() throws Exception {
			// given
			long userId = 1L;
			UserAdminUpdateStatusRequest request = new UserAdminUpdateStatusRequest(UserStatus.INACTIVE);
			String requestJson = objectMapper.writeValueAsString(request);

			// when & then
			mockMvc.perform(patch("/admin/users/{userId}/status", userId)
					.contentType(MediaType.APPLICATION_JSON)
					.content(requestJson)
					.with(csrf()))
				.andExpect(status().isOk())
				.andDo(print());

			then(userAdminService).should(times(1))
				.updateUserStatus(eq(userId), any(UserAdminUpdateStatusRequest.class));
		}
	}

	@Nested
	@DisplayName("유저 권한 변경 API 테스트")
	class UpdateUserRolesApiTest {

		@Test
		@WithMockUser(roles = "ADMIN")
		@DisplayName("성공")
		void success_updateUserRoles() throws Exception {
			// given
			long userId = 1L;
			UserAdminUpdateRolesRequest request = new UserAdminUpdateRolesRequest(
				Set.of(UserRole.ROLE_ADMIN, UserRole.ROLE_USER));
			String requestJson = objectMapper.writeValueAsString(request);

			// when & then
			mockMvc.perform(patch("/admin/users/{userId}/roles", userId)
					.contentType(MediaType.APPLICATION_JSON)
					.content(requestJson)
					.with(csrf()))
				.andExpect(status().isOk())
				.andDo(print());

			then(userAdminService).should(times(1)).updateUserRoles(eq(userId), any(UserAdminUpdateRolesRequest.class));
		}
	}
}
