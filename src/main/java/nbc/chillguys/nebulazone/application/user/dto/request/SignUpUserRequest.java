package nbc.chillguys.nebulazone.application.user.dto.request;

import java.util.Set;

public record SignUpUserRequest(
	String email,
	String password,
	String phone,
	String nickname,
	String profileImageUrl,
	Set<SignUpUserAddressRequest> addresses
) {
	public record SignUpUserAddressRequest(
		String roadAddress,
		String detailAddress,
		String addressNickname
	) {
	}
}
