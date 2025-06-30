package nbc.chillguys.nebulazone.infra.oauth.handler;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.common.util.CookieUtils;
import nbc.chillguys.nebulazone.infra.oauth.dto.CustomOAuth2User;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
	@Value("${frontend.url}")
	private String frontendUrl;

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
		Authentication authentication) throws IOException {
		CustomOAuth2User oAuth2User = (CustomOAuth2User)authentication.getPrincipal();

		oAuth2User.attributes().get("authorities");

		int maxAge = 2 * 24 * 60 * 60;
		Cookie cookie = CookieUtils.createCookie("Refresh_Token", oAuth2User.refreshToken(), maxAge);
		response.addCookie(cookie);

		String redirectUrl = frontendUrl + "/oauth/redirect"
			+ "?access_token=" + oAuth2User.accessToken();
		getRedirectStrategy().sendRedirect(request, response, redirectUrl);
	}
}
