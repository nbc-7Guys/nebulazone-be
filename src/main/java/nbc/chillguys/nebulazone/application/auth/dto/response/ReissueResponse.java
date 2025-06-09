package nbc.chillguys.nebulazone.application.auth.dto.response;

public record ReissueResponse(
	String accessToken
) {
	public static ReissueResponse from(String accessToken) {
		return new ReissueResponse(accessToken);
	}
}
