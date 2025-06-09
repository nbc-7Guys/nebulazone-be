package nbc.chillguys.nebulazone.domain.user.dto;

import nbc.chillguys.nebulazone.application.user.dto.request.UpdateUserRequest;

public record UserUpdateCommand(
	Long userId,
	String nickname,
	String oldPassword,
	String newPassword
) {
	public static UserUpdateCommand of(UpdateUserRequest updateUserRequest, Long userId) {
		return new UserUpdateCommand(
			userId,
			updateUserRequest.nickname(),
			updateUserRequest.passwordChangeForm().oldPassword(),
			updateUserRequest.passwordChangeForm().newPassword()
		);
	}
}
