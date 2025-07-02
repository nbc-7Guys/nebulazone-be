package nbc.chillguys.nebulazone.application.auth.dto.response;

import org.springframework.security.core.Authentication;

import nbc.chillguys.nebulazone.infra.security.dto.AuthTokens;

public record ReissueResponse(
	String accessToken,
	String tokenType,
	Long expiresIn,
	Authentication authentication
) {
	public static ReissueResponse of(AuthTokens authTokens, Authentication authentication) {
		return new ReissueResponse(
			authTokens.accessToken(),
			authTokens.tokenType(),
			authTokens.expiresIn(),
			authentication
		);
	}
}
