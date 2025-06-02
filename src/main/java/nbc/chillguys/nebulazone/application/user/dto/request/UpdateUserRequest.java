package nbc.chillguys.nebulazone.application.user.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UpdateUserRequest(
	String nickname,

	@Valid
	PasswordChangeForm passwordChangeForm
) {
	public record PasswordChangeForm(
		@NotBlank(message = "비밀번호는 필수 입력값입니다.")
		@Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_\\-+={\\[\\]}:;\"'<,>.?/])\\S{8,}$",
			message = "비밀번호는 대소문자, 숫자, 특수문자를 각각 최소 1자 이상 포함하며 8글자 이상이어야 합니다.")
		String oldPassword,

		@NotBlank(message = "새 비밀번호는 필수 입력값입니다.")
		@Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_\\-+={\\[\\]}:;\"'<,>.?/])\\S{8,}$",
			message = "비밀번호는 대소문자, 숫자, 특수문자를 각각 최소 1자 이상 포함하며 8글자 이상이어야 합니다.")
		String newPassword
	) {
	}
}
