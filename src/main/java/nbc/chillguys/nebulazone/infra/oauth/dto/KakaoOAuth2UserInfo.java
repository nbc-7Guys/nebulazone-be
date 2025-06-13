package nbc.chillguys.nebulazone.infra.oauth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import nbc.chillguys.nebulazone.domain.user.entity.OAuthType;

public record KakaoOAuth2UserInfo(
	Long id,
	@JsonProperty("kakao_account")
	KakaoAccount kakaoAccount
) implements OAuth2UserInfo {
	@Override
	public String getId() {
		return String.valueOf(id);
	}

	@Override
	public String getEmail() {
		return kakaoAccount.email();
	}

	@Override
	public String getNickname() {
		return kakaoAccount.profile.nickname;
	}

	@Override
	public String getProfileImageUrl() {
		return kakaoAccount.profile.profileImageUrl;
	}

	@Override
	public OAuthType getOAuthType() {
		return OAuthType.KAKAO;
	}

	public record KakaoAccount(
		String email,
		Profile profile
	) {
		public record Profile(
			String nickname,
			@JsonProperty("profile_image_url")
			String profileImageUrl
		) {
		}
	}
}
