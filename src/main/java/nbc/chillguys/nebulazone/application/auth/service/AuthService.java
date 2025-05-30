package nbc.chillguys.nebulazone.application.auth.service;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.application.auth.dto.request.SignInRequest;
import nbc.chillguys.nebulazone.application.auth.dto.response.SignInResponse;
import nbc.chillguys.nebulazone.domain.auth.vo.AuthUser;
import nbc.chillguys.nebulazone.domain.user.dto.UserSignInInfo;
import nbc.chillguys.nebulazone.domain.user.service.UserDomainService;
import nbc.chillguys.nebulazone.infra.security.jwt.JwtUtil;

@Service
@RequiredArgsConstructor
public class AuthService {
	private final UserDomainService userDomainService;
	private final JwtUtil jwtUtil;

	public SignInResponse signIn(SignInRequest signInRequest) {
		UserSignInInfo userSignInInfo = userDomainService.findActiveUserByEmail(signInRequest.email());

		userDomainService.validPassword(signInRequest.password(), userSignInInfo.password());

		AuthUser authUser = AuthUser.from(userSignInInfo);

		String accessToken = jwtUtil.generateAccessToken(authUser);
		String refreshToken = jwtUtil.generateRefreshToken(authUser);

		return SignInResponse.of(accessToken, refreshToken);
	}
}
