package nbc.chillguys.nebulazone.infra.oauth.handler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

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
		response.setHeader("Refresh-Token", "Bearer " + oAuth2User.refreshToken());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setCharacterEncoding(StandardCharsets.UTF_8.name());
		response.setStatus(HttpServletResponse.SC_OK);
		response.getWriter().print(objectMapper.writeValueAsString(TokensDto.builder()
			.accessToken(oAuth2User.accessToken())
			.refreshToken(oAuth2User.refreshToken())
			.build()));
	}
}
