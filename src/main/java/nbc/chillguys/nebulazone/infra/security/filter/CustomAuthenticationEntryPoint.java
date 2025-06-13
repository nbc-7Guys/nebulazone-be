package nbc.chillguys.nebulazone.infra.security.filter;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.util.StandardCharset;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nbc.chillguys.nebulazone.common.response.CommonResponse;
import nbc.chillguys.nebulazone.infra.security.filter.exception.JwtFilterException;

@Slf4j
@RequiredArgsConstructor
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {
	private final ObjectMapper objectMapper;

	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response,
		AuthenticationException authException) throws IOException {
		if (authException instanceof JwtFilterException jwtFilterException) {
			log.error("exception : {}", jwtFilterException.getMessage(), jwtFilterException);
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			response.setContentType(MediaType.APPLICATION_JSON_VALUE);
			response.setCharacterEncoding(StandardCharset.UTF_8.name());
			objectMapper.writeValue(response.getWriter(),
				CommonResponse.of(jwtFilterException.getStatus().value(), jwtFilterException.getMessage()));

			return;
		}

		log.error("exception : {}", authException.getMessage(), authException);
		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setCharacterEncoding(StandardCharset.UTF_8.name());
		objectMapper.writeValue(response.getWriter(),
			CommonResponse.of(HttpStatus.UNAUTHORIZED.value(), authException.getMessage()));
	}
}
