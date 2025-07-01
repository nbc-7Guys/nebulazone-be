package nbc.chillguys.nebulazone.infra.security.constant;

import lombok.AllArgsConstructor;

@AllArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public final class OAuth2CookieConstants {
	public static final String OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME = "oauth2_auth_request";
	public static final String REDIRECT_URI_PARAM_COOKIE_NAME = "redirect_uri";
	public static final int COOKIE_EXPIRE_SECONDS = 180;
}
