package nbc.chillguys.nebulazone.application.auth.service;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.application.auth.dto.request.SignInRequest;
import nbc.chillguys.nebulazone.application.auth.dto.response.ReissueResponse;
import nbc.chillguys.nebulazone.application.auth.dto.response.SignInResponse;
import nbc.chillguys.nebulazone.application.auth.metrics.TrackAuthMetrics;
import nbc.chillguys.nebulazone.domain.user.entity.User;
import nbc.chillguys.nebulazone.domain.user.service.UserDomainService;
import nbc.chillguys.nebulazone.infra.security.JwtUtil;
import nbc.chillguys.nebulazone.infra.security.dto.AuthTokens;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {
	private final UserDomainService userDomainService;
	private final JwtUtil jwtUtil;

	@TrackAuthMetrics
	public SignInResponse signIn(SignInRequest request) {
		long start = System.currentTimeMillis();

		User user = userDomainService.findActiveUserByEmail(request.email());
		userDomainService.validPassword(request.password(), user.getPassword());

		AuthTokens tokens = jwtUtil.generateTokens(user);
		return SignInResponse.of(tokens.accessToken(), tokens.refreshToken());
	}

	public void signOut() {
		SecurityContextHolder.clearContext();
	}

	public ReissueResponse reissueAccessToken(String refreshToken) {
		String accessToken = jwtUtil.regenerateAccessToken(refreshToken);

		User user = jwtUtil.getUserFromToken(refreshToken);

		Authentication authentication = new UsernamePasswordAuthenticationToken(
			user, accessToken, user.getAuthorities()
		);

		return ReissueResponse.of(accessToken, authentication);
	}
}
