package nbc.chillguys.nebulazone.infra.oauth.handler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.infra.oauth.dto.CustomOAuth2User;
import nbc.chillguys.nebulazone.infra.oauth.dto.TokensDto;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {
	private final ObjectMapper objectMapper;

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
		Authentication authentication) throws IOException {
		CustomOAuth2User oAuth2User = (CustomOAuth2User)authentication.getPrincipal();

		oAuth2User.attributes().get("authorities");

		response.setHeader("Authorization", "Bearer " + oAuth2User.accessToken());
		Cookie cookie = new Cookie("Refresh_Token", oAuth2User.refreshToken());
		cookie.setHttpOnly(true);
		// https 통신이 아니기 때문에 임시 주석
		// cookie.setSecure(true);
		cookie.setPath("/");
		cookie.setMaxAge(2 * 24 * 60 * 60);
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setCharacterEncoding(StandardCharsets.UTF_8.name());
		response.setStatus(HttpServletResponse.SC_OK);
		response.getWriter().print(objectMapper.writeValueAsString(TokensDto.builder()
			.accessToken(oAuth2User.accessToken())
			.refreshToken(oAuth2User.refreshToken())
			.build()));
	}
}
