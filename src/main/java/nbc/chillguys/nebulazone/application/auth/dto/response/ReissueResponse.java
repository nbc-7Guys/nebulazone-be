package nbc.chillguys.nebulazone.application.auth.dto.response;

import org.springframework.security.core.Authentication;

public record ReissueResponse(
	String accessToken,
	Authentication authentication
) {
	public static ReissueResponse of(String accessToken, Authentication authentication) {
		return new ReissueResponse(
			accessToken,
			authentication
		);
	}
}
