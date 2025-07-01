package nbc.chillguys.nebulazone.infra.security.config;

import org.apache.commons.lang3.StringUtils;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import nbc.chillguys.nebulazone.common.util.CookieUtils;
import nbc.chillguys.nebulazone.infra.security.constant.OAuth2CookieConstants;

@Component
public class HttpCookieOAuth2AuthorizationRequestRepository implements
	AuthorizationRequestRepository<OAuth2AuthorizationRequest> {
	@Override
	public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
		return CookieUtils.getCookie(request, OAuth2CookieConstants.OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME)
			.map(cookie -> CookieUtils.deserialize(cookie, OAuth2AuthorizationRequest.class))
			.orElse(null);
	}

	@Override
	public void saveAuthorizationRequest(OAuth2AuthorizationRequest authorizationRequest, HttpServletRequest request,
		HttpServletResponse response) {
		if (authorizationRequest == null) {
			CookieUtils.deleteCookie(response, OAuth2CookieConstants.OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME);
			CookieUtils.deleteCookie(response, OAuth2CookieConstants.REDIRECT_URI_PARAM_COOKIE_NAME);
			return;
		}

		Cookie cookie = CookieUtils.createCookie(OAuth2CookieConstants.OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME,
			CookieUtils.serialize(authorizationRequest), OAuth2CookieConstants.COOKIE_EXPIRE_SECONDS);
		cookie.setAttribute("SameSite", "None");
		response.addCookie(cookie);

		String redirectUriAfterLogin = request.getParameter(OAuth2CookieConstants.REDIRECT_URI_PARAM_COOKIE_NAME);
		if (StringUtils.isNotBlank(redirectUriAfterLogin)) {
			CookieUtils.createCookie(OAuth2CookieConstants.REDIRECT_URI_PARAM_COOKIE_NAME, redirectUriAfterLogin,
				OAuth2CookieConstants.COOKIE_EXPIRE_SECONDS);
		}
	}

	@Override
	public OAuth2AuthorizationRequest removeAuthorizationRequest(HttpServletRequest request,
		HttpServletResponse response) {
		OAuth2AuthorizationRequest oAuth2AuthorizationRequest = loadAuthorizationRequest(request);
		removeAuthorizationRequestCookies(response);
		return oAuth2AuthorizationRequest;
	}

	public void removeAuthorizationRequestCookies(HttpServletResponse response) {
		CookieUtils.deleteCookie(response, OAuth2CookieConstants.OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME);
		CookieUtils.deleteCookie(response, OAuth2CookieConstants.REDIRECT_URI_PARAM_COOKIE_NAME);
	}
}
