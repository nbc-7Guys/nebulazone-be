package nbc.chillguys.nebulazone.application.auth.dto.response;

public record RegenerateAccessTokenResponse(
	String accessToken,
	String tokenType,
	Long expiresIn
) {
	public static RegenerateAccessTokenResponse from(ReissueResponse reissueResponse) {
		return new RegenerateAccessTokenResponse(
			reissueResponse.accessToken(),
			reissueResponse.tokenType(),
			reissueResponse.expiresIn()
		);
	}
}
