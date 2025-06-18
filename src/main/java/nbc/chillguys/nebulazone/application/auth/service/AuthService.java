package nbc.chillguys.nebulazone.application.auth.service;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.application.auth.dto.request.SignInRequest;
import nbc.chillguys.nebulazone.application.auth.dto.response.ReissueResponse;
import nbc.chillguys.nebulazone.application.auth.dto.response.SignInResponse;
import nbc.chillguys.nebulazone.domain.user.entity.User;
import nbc.chillguys.nebulazone.domain.user.service.UserDomainService;
import nbc.chillguys.nebulazone.infra.security.JwtUtil;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {
	private final UserDomainService userDomainService;
	private final JwtUtil jwtUtil;

	public SignInResponse signIn(SignInRequest signInRequest) {
		User user = userDomainService.findActiveUserByEmail(signInRequest.email());

		userDomainService.validPassword(signInRequest.password(), user.getPassword());

		String accessToken = jwtUtil.generateAccessToken(user);
		String refreshToken = jwtUtil.generateRefreshToken(user);

		return SignInResponse.of(accessToken, refreshToken);
	}

	public void signOut() {
		SecurityContextHolder.clearContext();
	}

	public ReissueResponse reissueAccessToken(String refreshToken) {
		String accessToken = jwtUtil.regenerateAccessToken(refreshToken);

		return ReissueResponse.from(accessToken);
	}
}
