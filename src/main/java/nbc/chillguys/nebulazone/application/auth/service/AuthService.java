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
import nbc.chillguys.nebulazone.application.auth.metrics.AuthMetrics;
import nbc.chillguys.nebulazone.common.exception.BaseException;
import nbc.chillguys.nebulazone.domain.user.entity.User;
import nbc.chillguys.nebulazone.domain.user.service.UserDomainService;
import nbc.chillguys.nebulazone.infra.security.JwtUtil;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {
	private final UserDomainService userDomainService;
	private final JwtUtil jwtUtil;
	private final AuthMetrics authMetrics;

	public SignInResponse signIn(SignInRequest request) {
		long start = System.currentTimeMillis();

		try {
			User user = userDomainService.findActiveUserByEmail(request.email());
			userDomainService.validPassword(request.password(), user.getPassword());

			authMetrics.countSuccess();
			return SignInResponse.of(
				jwtUtil.generateTokens(user).accessToken(),
				jwtUtil.generateTokens(user).refreshToken()
			);

		} catch (BaseException e) {
			authMetrics.countFailure();
			throw e;

		} finally {
			authMetrics.recordLatency(System.currentTimeMillis() - start);
		}
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
