package nbc.chillguys.nebulazone.infra.security.dto;

public record AuthTokens(
	String accessToken,
	String refreshToken,
	String tokenType,
	Long expiresIn,
	Long refreshExpiresIn
) {
	public static AuthTokens of(String accessToken, String refreshToken, String tokenType, Long expiresIn,
		Long refreshExpiresIn) {
		return new AuthTokens(
			accessToken,
			refreshToken,
			tokenType,
			expiresIn,
			refreshExpiresIn
		);
	}
}
