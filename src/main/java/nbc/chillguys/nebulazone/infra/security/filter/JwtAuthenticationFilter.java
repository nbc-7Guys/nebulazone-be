package nbc.chillguys.nebulazone.infra.security.filter;

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
import nbc.chillguys.nebulazone.infra.security.JwtUtil;
import nbc.chillguys.nebulazone.infra.security.constant.JwtConstants;
import nbc.chillguys.nebulazone.infra.security.filter.exception.JwtFilterErrorCode;
import nbc.chillguys.nebulazone.infra.security.filter.exception.JwtFilterException;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
	private final JwtUtil jwtUtil;
	private final CustomAuthenticationEntryPoint entryPoint;

	@Override
	protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
		@NonNull FilterChain filterChain) throws ServletException, IOException {
		try {
			String accessHeader = request.getHeader(JwtConstants.AUTH_HEADER);
			if (accessHeader != null && !accessHeader.isBlank()) {
				String accessToken = resolveToken(accessHeader);
				if (jwtUtil.isTokenExpired(accessToken)) {
					throw new JwtFilterException(JwtFilterErrorCode.TOKEN_EXPIRED);
				}

				AuthUser authUser = jwtUtil.getAuthUserFromToken(accessToken);
				Authentication authentication = new UsernamePasswordAuthenticationToken(authUser, accessToken,
					authUser.getAuthorities());

				SecurityContextHolder.getContext().setAuthentication(authentication);
			}

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
