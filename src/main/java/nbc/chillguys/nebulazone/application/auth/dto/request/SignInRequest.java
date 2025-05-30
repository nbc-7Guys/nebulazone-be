package nbc.chillguys.nebulazone.application.auth.dto.request;

public record SignInRequest(
	String email,
	String password
) {
}
