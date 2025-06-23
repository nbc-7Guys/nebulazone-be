package nbc.chillguys.nebulazone.application.auth.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.application.auth.dto.request.SignInRequest;
import nbc.chillguys.nebulazone.application.auth.dto.response.RegenerateAccessTokenResponse;
import nbc.chillguys.nebulazone.application.auth.dto.response.ReissueResponse;
import nbc.chillguys.nebulazone.application.auth.dto.response.SignInResponse;
import nbc.chillguys.nebulazone.application.auth.service.AuthService;
import nbc.chillguys.nebulazone.common.util.CookieUtils;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
	private final AuthService authService;

	@PostMapping("/signin")
	public ResponseEntity<SignInResponse> signIn(
		@Valid @RequestBody SignInRequest signInRequestDto,
		HttpServletResponse httpServletResponse
	) {
		SignInResponse response = authService.signIn(signInRequestDto);

		int maxAge = 2 * 24 * 60 * 60;
		Cookie cookie = CookieUtils.createCookie("Refresh_Token", response.refreshToken(), maxAge);
		httpServletResponse.addCookie(cookie);

		return ResponseEntity.ok(response);
	}

	@PostMapping("/signout")
	public ResponseEntity<String> signOut(HttpServletResponse httpServletResponse) {
		authService.signOut();

		CookieUtils.deleteCookie(httpServletResponse, "Refresh_Token");

		return ResponseEntity.ok("로그아웃 성공");
	}

	@PostMapping("/reissue")
	public ResponseEntity<RegenerateAccessTokenResponse> reissueAccessToken(
		@CookieValue("Refresh_Token") String refreshToken
	) {
		ReissueResponse response = authService.reissueAccessToken(refreshToken);

		SecurityContextHolder.getContext().setAuthentication(response.authentication());

		return ResponseEntity.ok(RegenerateAccessTokenResponse.from(response.accessToken()));
	}
}
