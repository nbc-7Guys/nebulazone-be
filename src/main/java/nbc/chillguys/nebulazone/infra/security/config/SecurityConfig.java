package nbc.chillguys.nebulazone.infra.security.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.fasterxml.jackson.databind.ObjectMapper;

import nbc.chillguys.nebulazone.infra.oauth.handler.OAuth2SuccessHandler;
import nbc.chillguys.nebulazone.infra.oauth.service.OAuthService;
import nbc.chillguys.nebulazone.infra.security.JwtUtil;
import nbc.chillguys.nebulazone.infra.security.filter.CustomAuthenticationEntryPoint;
import nbc.chillguys.nebulazone.infra.security.filter.JwtAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
	private final CustomAuthenticationEntryPoint entryPoint;
	private final JwtAuthenticationFilter jwtAuthenticationFilter;
	private final OAuthService oAuthService;
	private final OAuth2SuccessHandler oAuth2SuccessHandler;

	public SecurityConfig(ObjectMapper objectMapper, JwtUtil jwtUtil, OAuthService oAuthService,
		OAuth2SuccessHandler oAuth2SuccessHandler) {
		this.entryPoint = new CustomAuthenticationEntryPoint(objectMapper);
		this.jwtAuthenticationFilter = new JwtAuthenticationFilter(jwtUtil, entryPoint);
		this.oAuthService = oAuthService;
		this.oAuth2SuccessHandler = oAuth2SuccessHandler;
	}

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
		return httpSecurity
			.cors(cors -> cors.configurationSource(corsConfigurationSource()))
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
					"/api/v1/**",
					"/ws/**",
					"/ws",
					"/chat/**",
					"/topic/**"
				).permitAll()
				.requestMatchers(
					"/auth/signin",
					"/users/signup",
					"/oauth2/**"
				).permitAll()
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
				.successHandler(oAuth2SuccessHandler))
			.exceptionHandling(exception ->
				exception.authenticationEntryPoint(entryPoint))
			.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
			.build();
	}

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowedOriginPatterns(List.of("*"));
		configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
		configuration.setAllowedHeaders(List.of("*"));
		configuration.setExposedHeaders(List.of("*"));
		configuration.setAllowCredentials(true);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration auth) throws Exception {
		return auth.getAuthenticationManager();
	}
}
