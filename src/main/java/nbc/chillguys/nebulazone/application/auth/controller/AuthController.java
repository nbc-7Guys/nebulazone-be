package nbc.chillguys.nebulazone.application.auth.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.application.auth.dto.request.SignInRequest;
import nbc.chillguys.nebulazone.application.auth.dto.response.ReissueResponse;
import nbc.chillguys.nebulazone.application.auth.dto.response.SignInResponse;
import nbc.chillguys.nebulazone.application.auth.service.AuthService;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
	private final AuthService authService;

	@PostMapping("/signin")
	public ResponseEntity<SignInResponse> signIn(
		@Valid @RequestBody SignInRequest signInRequestDto
	) {
		SignInResponse response = authService.signIn(signInRequestDto);

		return ResponseEntity.ok(response);
	}

	@PostMapping("/signout")
	public ResponseEntity<String> signOut() {
		authService.signOut();

		return ResponseEntity.ok("로그아웃 성공");
	}

	@PostMapping("/reissue")
	public ResponseEntity<ReissueResponse> reissueAccessToken(
		@RequestHeader("Refresh-Token") String refreshToken
	) {
		ReissueResponse response = authService.reissueAccessToken(refreshToken);

		return ResponseEntity.ok(response);
	}
}
