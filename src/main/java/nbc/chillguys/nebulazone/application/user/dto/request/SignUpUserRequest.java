package nbc.chillguys.nebulazone.application.user.dto.request;

import java.util.Set;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SignUpUserRequest(
	@NotBlank(message = "이메일은 필수 입력값입니다.")
	@Email(message = "이메일 형식이 잘못되었습니다.")
	String email,

	@NotBlank(message = "비밀번호는 필수 입력값입니다.")
	@Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_\\-+={\\[\\]}:;\"'<,>.?/])\\S{8,}$",
		message = "비밀번호는 대소문자, 숫자, 특수문자를 각각 최소 1자 이상 포함하며 8글자 이상이어야 합니다.")
	String password,

	@NotBlank(message = "전화번호는 필수 입력값입니다.")
	@Pattern(
		regexp = "^01(?:0|1|[6-9])[-]?(?:\\d{3}|\\d{4})[-]?\\d{4}$",
		message = "전화번호 형식이 잘못되었습니다. (예: 010-1234-5678 또는 01012345678)"
	)
	String phone,

	@NotBlank(message = "닉네임은 필수 입력값입니다.")
	@Pattern(
		regexp = "^[가-힣a-zA-Z0-9]{2,10}$",
		message = "닉네임은 2자 이상 10자 이하의 한글, 영문, 숫자만 사용 가능합니다."
	)
	String nickname,

	@Valid
	@NotNull(message = "주소는 필수 입력값이니다.")
	Set<SignUpUserAddressRequest> addresses
) {
	public record SignUpUserAddressRequest(
		@NotBlank(message = "도로명 주소는 필수 입력값입니다.")
		String roadAddress,

		@NotBlank(message = "상세 주소는 필수 입력값입니다.")
		String detailAddress,

		@NotBlank(message = "주소 별칭은 필수 입력값입니다.")
		@Size(min = 1, max = 20, message = "주소 별칭은 1자 이상 20자 이하여야 합니다.")
		String addressNickname
	) {
	}
}
