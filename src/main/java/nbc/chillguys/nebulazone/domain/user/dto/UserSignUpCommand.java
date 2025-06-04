package nbc.chillguys.nebulazone.domain.user.dto;

import java.util.Set;
import java.util.stream.Collectors;

import lombok.Builder;
import nbc.chillguys.nebulazone.application.user.dto.request.SignUpUserRequest;
import nbc.chillguys.nebulazone.domain.user.entity.Address;
import nbc.chillguys.nebulazone.domain.user.entity.OAuthType;
import nbc.chillguys.nebulazone.infra.oauth.dto.OAuth2UserInfo;

@Builder
public record UserSignUpCommand(
	String email,
	String password,
	String phone,
	String nickname,
	String profileImageUrl,
	Set<Address> addresses,
	OAuthType oAuthType,
	String oauthId
) {
	public static UserSignUpCommand of(SignUpUserRequest signUpUserRequest, String profileImageUrl) {
		return UserSignUpCommand.builder()
			.email(signUpUserRequest.email())
			.password(signUpUserRequest.password())
			.nickname(signUpUserRequest.nickname())
			.phone(signUpUserRequest.phone())
			.profileImageUrl(profileImageUrl)
			.addresses(signUpUserRequest.addresses().stream()
				.map(a -> Address.builder()
					.roadAddress(a.roadAddress())
					.detailAddress(a.detailAddress())
					.addressNickname(a.addressNickname())
					.build())
				.collect(Collectors.toSet()))
			.oAuthType(OAuthType.DOMAIN)
			.build();
	}

	public static UserSignUpCommand of(OAuth2UserInfo oAuth2UserInfo) {
		return UserSignUpCommand.builder()
			.email(oAuth2UserInfo.getEmail())
			.nickname(oAuth2UserInfo.getNickname())
			.profileImageUrl(oAuth2UserInfo.getProfileImageUrl())
			.oAuthType(oAuth2UserInfo.getOAuthType())
			.oauthId(oAuth2UserInfo.getId())
			.build();
	}
}
