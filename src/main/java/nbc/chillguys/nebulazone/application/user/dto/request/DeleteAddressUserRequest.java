package nbc.chillguys.nebulazone.application.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DeleteAddressUserRequest(
	@NotBlank(message = "도로명 주소는 필수 입력값입니다.")
	String roadAddress,

	@NotBlank(message = "상세 주소는 필수 입력값입니다.")
	String detailAddress,

	@NotBlank(message = "주소 별칭은 필수 입력값입니다.")
	@Size(min = 1, max = 20, message = "주소 별칭은 1자 이상 20자 이하여야 합니다.")
	String addressNickname
) {
}
