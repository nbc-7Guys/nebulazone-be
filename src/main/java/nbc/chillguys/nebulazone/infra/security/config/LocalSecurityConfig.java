package nbc.chillguys.nebulazone.infra.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.infra.oauth.handler.OAuth2SuccessHandler;
import nbc.chillguys.nebulazone.infra.oauth.service.OAuthService;
import nbc.chillguys.nebulazone.infra.security.filter.BanCheckFilter;
import nbc.chillguys.nebulazone.infra.security.filter.CustomAuthenticationEntryPoint;
import nbc.chillguys.nebulazone.infra.security.filter.JwtAuthenticationFilter;
import nbc.chillguys.nebulazone.infra.security.filter.LoggingFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class LocalSecurityConfig {
	private final CustomAuthenticationEntryPoint entryPoint;
	private final JwtAuthenticationFilter jwtAuthenticationFilter;
	private final OAuthService oAuthService;
	private final OAuth2SuccessHandler oAuth2SuccessHandler;
	private final LoggingFilter loggingFilter;
	private final BanCheckFilter banCheckFilter;
	private final HttpCookieOAuth2AuthorizationRequestRepository httpCookieOAuth2AuthorizationRequestRepository;

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
		return httpSecurity
			.cors(Customizer.withDefaults())
			.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.csrf(AbstractHttpConfigurer::disable)
			.authorizeHttpRequests(auth -> auth
				.requestMatchers(
					"/v3/api-docs/**",
					"/swagger-ui/**",
					"/swagger-ui.html",
					"/webjars/**",
					"/swagger-resources/**",
					"/favicon.ico",
					"/error",
					"/actuator/**",
					"/metrics/**",
					"/ws/**",
					"/ws",
					"/chat/**",
					"/topic/**",
					"/internal/bans"
				).permitAll()
				.requestMatchers(
					"/auth/signin",
					"/users/signup",
					"/auth/reissue",
					"/oauth2/**"
				).permitAll()
				.requestMatchers("/admin/**").hasRole("ADMIN")
				.requestMatchers(
					HttpMethod.GET,
					"/auctions/**",
					"/catalogs/**",
					"/products/**"
				).permitAll()
				.anyRequest().authenticated())
			.oauth2Login(oauth2 -> oauth2
				.userInfoEndpoint(userInfo -> userInfo
					.userService(oAuthService))
				.successHandler(oAuth2SuccessHandler)
				.authorizationEndpoint(authorization -> authorization
					.authorizationRequestRepository(httpCookieOAuth2AuthorizationRequestRepository)
				)
			)
			.exceptionHandling(exception ->
				exception.authenticationEntryPoint(entryPoint))
			.addFilterBefore(banCheckFilter, UsernamePasswordAuthenticationFilter.class)
			.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
			.addFilterBefore(loggingFilter, UsernamePasswordAuthenticationFilter.class)
			.build();
	}
}
