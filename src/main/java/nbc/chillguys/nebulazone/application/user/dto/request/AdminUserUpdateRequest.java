package nbc.chillguys.nebulazone.application.user.dto.request;

public record AdminUserUpdateRequest(
	String email,
	String phone,
	String nickname,
	String profileImage
) {
}
