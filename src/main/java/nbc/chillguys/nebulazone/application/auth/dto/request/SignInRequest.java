package nbc.chillguys.nebulazone.application.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record SignInRequest(
	@NotBlank(message = "이메일은 필수 입력값입니다.")
	@Email(message = "이메일 형식이 잘못되었습니다.")
	String email,

	@NotBlank(message = "비밀번호는 필수 입력값입니다.")
	@Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_\\-+={\\[\\]}:;\"'<,>.?/])\\S{8,}$",
		message = "비밀번호는 대소문자, 숫자, 특수문자를 각각 최소 1자 이상 포함하며 8글자 이상이어야 합니다.")
	String password
) {
}
