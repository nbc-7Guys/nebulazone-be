package nbc.chillguys.nebulazone.application.auth.dto.response;

public record RegenerateAccessTokenResponse(
	String accessToken
) {
	public static RegenerateAccessTokenResponse from(String accessToken) {
		return new RegenerateAccessTokenResponse(
			accessToken
		);
	}
}
