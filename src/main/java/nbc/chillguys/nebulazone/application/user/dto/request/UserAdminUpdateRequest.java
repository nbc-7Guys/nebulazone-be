package nbc.chillguys.nebulazone.application.user.dto.request;

public record UserAdminUpdateRequest(
	String email,
	String phone,
	String nickname,
	String profileImage
) {
}
