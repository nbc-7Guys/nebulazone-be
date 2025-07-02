package nbc.chillguys.nebulazone.application.auth.dto.response;

import nbc.chillguys.nebulazone.infra.security.dto.AuthTokens;

public record SignInResponse(
	String accessToken,
	String refreshToken,
	String tokenType,
	Long expiresIn,
	Long refreshExpiresIn
) {
	public static SignInResponse from(AuthTokens authTokens) {
		return new SignInResponse(
			authTokens.accessToken(),
			authTokens.refreshToken(),
			authTokens.tokenType(),
			authTokens.expiresIn(),
			authTokens.refreshExpiresIn()
		);
	}
}
