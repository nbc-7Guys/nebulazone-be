package nbc.chillguys.nebulazone.infra.security.jwt.filter;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.domain.auth.vo.AuthUser;
import nbc.chillguys.nebulazone.infra.security.jwt.JwtUtil;
import nbc.chillguys.nebulazone.infra.security.jwt.constant.JwtConstants;
import nbc.chillguys.nebulazone.infra.security.jwt.filter.exception.JwtFilterErrorCode;
import nbc.chillguys.nebulazone.infra.security.jwt.filter.exception.JwtFilterException;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
	private final JwtUtil jwtUtil;
	private final CustomAuthenticationEntryPoint entryPoint;

	@Override
	protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
		@NonNull FilterChain filterChain) throws ServletException, IOException {
		String uri = request.getRequestURI();

		if (JwtConstants.PUBLIC_URLS.stream().anyMatch(uri::startsWith)) {
			filterChain.doFilter(request, response);
			return;
		}

		try {
			String accessHeader = request.getHeader(JwtConstants.AUTH_HEADER);
			if (accessHeader == null || accessHeader.isBlank()) {
				throw new JwtFilterException(JwtFilterErrorCode.EMPTY_TOKEN);
			}

			String accessToken = resolveToken(accessHeader);
			if (jwtUtil.isTokenExpired(accessToken)) {
				String refreshHeader = request.getHeader(JwtConstants.REFRESH_HEADER);
				if (refreshHeader == null || refreshHeader.isBlank()) {
					throw new JwtFilterException(JwtFilterErrorCode.TOKEN_EXPIRED);
				}

				String refreshToken = resolveToken(refreshHeader);
				if (jwtUtil.isTokenExpired(refreshToken)) {
					throw new JwtFilterException(JwtFilterErrorCode.TOKEN_EXPIRED);
				}

				accessToken = jwtUtil.regenerateAccessToken(refreshToken);
				response.setHeader(JwtConstants.AUTH_HEADER, accessToken);
			}

			AuthUser authUser = jwtUtil.getAuthUserFromToken(accessToken);
			Authentication authentication = new UsernamePasswordAuthenticationToken(authUser, accessToken,
				authUser.getAuthorities());

			SecurityContextHolder.getContext().setAuthentication(authentication);

			filterChain.doFilter(request, response);
		} catch (JwtFilterException e) {
			SecurityContextHolder.clearContext();
			entryPoint.commence(request, response, e);
		}
	}

	private String resolveToken(String authorization) {
		if (!authorization.startsWith(JwtConstants.TOKEN_PREFIX)) {
			throw new JwtFilterException(JwtFilterErrorCode.MALFORMED_JWT_REQUEST);
		}

		return authorization.substring(JwtConstants.TOKEN_PREFIX.length());
	}
}
