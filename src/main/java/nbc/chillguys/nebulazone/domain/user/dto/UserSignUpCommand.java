package nbc.chillguys.nebulazone.domain.user.dto;

import java.util.Set;
import java.util.stream.Collectors;

import nbc.chillguys.nebulazone.application.user.dto.request.SignUpUserRequest;
import nbc.chillguys.nebulazone.domain.user.entity.Address;

public record UserSignUpCommand(
	String email,
	String password,
	String phone,
	String nickname,
	String profileImageUrl,
	Set<Address> addresses
) {
	public static UserSignUpCommand of(SignUpUserRequest signUpUserRequest, String profileImageUrl) {
		return new UserSignUpCommand(
			signUpUserRequest.email(),
			signUpUserRequest.password(),
			signUpUserRequest.phone(),
			signUpUserRequest.nickname(),
			profileImageUrl,
			signUpUserRequest.addresses().stream()
				.map(a -> Address.builder()
					.roadAddress(a.roadAddress())
					.detailAddress(a.detailAddress())
					.addressNickname(a.addressNickname())
					.build())
				.collect(Collectors.toSet())
		);
	}
}
