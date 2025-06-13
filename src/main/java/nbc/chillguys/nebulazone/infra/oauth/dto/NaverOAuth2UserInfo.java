package nbc.chillguys.nebulazone.infra.oauth.dto;

import nbc.chillguys.nebulazone.domain.user.entity.OAuthType;

public record NaverOAuth2UserInfo(
	Response response
) implements OAuth2UserInfo {
	@Override
	public String getId() {
		return response.id;
	}

	@Override
	public String getEmail() {
		return response.email;
	}

	@Override
	public String getNickname() {
		return response.nickname;
	}

	@Override
	public String getProfileImageUrl() {
		return response.profile_image;
	}

	@Override
	public OAuthType getOAuthType() {
		return OAuthType.NAVER;
	}

	public record Response(
		String id,
		String email,
		String nickname,
		String profile_image,
		String mobile
	) {
	}
}
