package nbc.chillguys.nebulazone.infra.oauth.dto;

import nbc.chillguys.nebulazone.domain.user.entity.OAuthType;

public interface OAuth2UserInfo {
	String getId();

	String getEmail();

	String getNickname();

	String getProfileImageUrl();

	OAuthType getOAuthType();
}
