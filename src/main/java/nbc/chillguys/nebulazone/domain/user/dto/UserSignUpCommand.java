package nbc.chillguys.nebulazone.domain.user.dto;

import java.util.List;

import nbc.chillguys.nebulazone.application.user.dto.request.SignUpUserRequest;
import nbc.chillguys.nebulazone.domain.user.entity.Address;
import nbc.chillguys.nebulazone.domain.user.entity.OAuthType;
import nbc.chillguys.nebulazone.infra.oauth.dto.OAuth2UserInfo;

public record UserSignUpCommand(
	String email,
	String password,
	String phone,
	String nickname,
	String profileImageUrl,
	List<Address> addresses,
	OAuthType oAuthType,
	String oauthId
) {
	public static UserSignUpCommand from(SignUpUserRequest signUpUserRequest) {
		return new UserSignUpCommand(
			signUpUserRequest.email(),
			signUpUserRequest.password(),
			signUpUserRequest.phone(),
			signUpUserRequest.nickname(),
			null,
			signUpUserRequest.addresses().stream()
				.map(a -> Address.builder()
					.roadAddress(a.roadAddress())
					.detailAddress(a.detailAddress())
					.addressNickname(a.addressNickname())
					.build())
				.toList(),
			OAuthType.DOMAIN,
			null
		);
	}

	public static UserSignUpCommand from(OAuth2UserInfo oAuth2UserInfo) {
		return new UserSignUpCommand(
			oAuth2UserInfo.getEmail(),
			null,
			null,
			oAuth2UserInfo.getNickname(),
			oAuth2UserInfo.getProfileImageUrl(),
			null,
			oAuth2UserInfo.getOAuthType(),
			oAuth2UserInfo.getId()
		);
	}
}
