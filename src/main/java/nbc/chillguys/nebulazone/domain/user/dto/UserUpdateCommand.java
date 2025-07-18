package nbc.chillguys.nebulazone.domain.user.dto;

import nbc.chillguys.nebulazone.application.user.dto.request.UpdateUserRequest;
import nbc.chillguys.nebulazone.domain.user.entity.User;

public record UserUpdateCommand(
	User user,
	String nickname,
	String oldPassword,
	String newPassword
) {
	public static UserUpdateCommand of(UpdateUserRequest updateUserRequest, User user) {
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
			user,
			updateUserRequest.nickname(),
			oldPassword,
			newPassword
		);
	}
}
