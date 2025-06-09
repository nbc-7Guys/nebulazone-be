package nbc.chillguys.nebulazone.application.auth.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.fasterxml.jackson.databind.ObjectMapper;

import nbc.chillguys.nebulazone.application.auth.dto.request.SignInRequest;
import nbc.chillguys.nebulazone.application.auth.dto.response.ReissueResponse;
import nbc.chillguys.nebulazone.application.auth.dto.response.SignInResponse;
import nbc.chillguys.nebulazone.application.auth.service.AuthService;
import nbc.chillguys.nebulazone.config.TestSecurityConfig;
import nbc.chillguys.nebulazone.support.mockuser.WithCustomMockUser;

@DisplayName("auth 컨트롤러 단위 테스트")
@Import(TestSecurityConfig.class)
@WebMvcTest(AuthController.class)
class AuthControllerTest {
	@MockitoBean
	private AuthService authService;

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	@DisplayName("로그인 성공")
	void success_signIn() throws Exception {
		// Given
		SignInRequest request = new SignInRequest("test@test.com", "encodedPassword1!");
		SignInResponse response = new SignInResponse("test_access_token", "test_refresh_token");

		given(authService.signIn(any()))
			.willReturn(response);

		// When
		ResultActions perform = mockMvc.perform(post("/auth/signin")
			.contentType(MediaType.APPLICATION_JSON)
			.content(objectMapper.writeValueAsString(request)));

		// Then
		perform.andDo(print())
			.andExpectAll(
				status().isOk(),
				jsonPath("$.accessToken")
					.value("test_access_token"),
				jsonPath("$.refreshToken")
					.value("test_refresh_token")
			);

	}

	@Test
	@WithCustomMockUser
	@DisplayName("로그 아웃 성공")
	void success_signOut() throws Exception {
		// Given

		// When
		ResultActions perform = mockMvc.perform(post("/auth/signout"));

		// Then
		perform.andDo(print())
			.andExpectAll(
				status().isOk(),
				jsonPath("$")
					.value("로그아웃 성공")
			);

	}

	@Test
	@DisplayName("access token 재발급 성공")
	void success_reissueAccessToken() throws Exception {
		// Given
		ReissueResponse response = new ReissueResponse("regenerateAccessToken");

		given(authService.reissueAccessToken(anyString()))
			.willReturn(response);

		// When
		ResultActions perform = mockMvc.perform(post("/auth/reissue")
			.header("Refresh-Token", "Bearer test_refresh_token"));

		// Then
		perform.andDo(print())
			.andExpectAll(
				status().isOk(),
				jsonPath("$.accessToken")
					.value("regenerateAccessToken")
			);

	}

}
