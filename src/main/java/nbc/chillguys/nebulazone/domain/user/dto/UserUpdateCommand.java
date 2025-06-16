package nbc.chillguys.nebulazone.domain.user.dto;

import nbc.chillguys.nebulazone.application.user.dto.request.UpdateUserRequest;

public record UserUpdateCommand(
	Long userId,
	String nickname,
	String oldPassword,
	String newPassword
) {
	public static UserUpdateCommand of(UpdateUserRequest updateUserRequest, Long userId) {
		String oldPassword;
		String newPassword;
		if (updateUserRequest.passwordChangeForm() == null) {
			oldPassword = null;
			newPassword = null;
		} else {
			oldPassword = updateUserRequest.passwordChangeForm().oldPassword();
			newPassword = updateUserRequest.passwordChangeForm().newPassword();
		}

		return new UserUpdateCommand(
			userId,
			updateUserRequest.nickname(),
			oldPassword,
			newPassword
		);
	}
}
