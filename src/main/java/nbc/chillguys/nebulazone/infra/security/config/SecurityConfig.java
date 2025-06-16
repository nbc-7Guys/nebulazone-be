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

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.domain.user.service.UserDomainService;
import nbc.chillguys.nebulazone.infra.oauth.handler.OAuth2SuccessHandler;
import nbc.chillguys.nebulazone.infra.oauth.service.OAuthService;
import nbc.chillguys.nebulazone.infra.security.JwtUtil;
import nbc.chillguys.nebulazone.infra.security.filter.CustomAuthenticationEntryPoint;
import nbc.chillguys.nebulazone.infra.security.filter.JwtAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
	// private final CustomAuthenticationEntryPoint entryPoint;
	// private final JwtAuthenticationFilter jwtAuthenticationFilter;
	// private final OAuthService oAuthService;
	private final OAuth2SuccessHandler oAuth2SuccessHandler;
	private final UserDomainService userDomainService;
	private final JwtUtil jwtUtil;
	private final ObjectMapper objectMapper;

	// public SecurityConfig(ObjectMapper objectMapper, JwtUtil jwtUtil, OAuthService oAuthService,
	// 	OAuth2SuccessHandler oAuth2SuccessHandler) {
	// 	this.entryPoint = new CustomAuthenticationEntryPoint(objectMapper);
	// 	this.jwtAuthenticationFilter = new JwtAuthenticationFilter(jwtUtil, entryPoint);
	// 	this.oAuthService = oAuthService;
	// 	this.oAuth2SuccessHandler = oAuth2SuccessHandler;
	// }

	@Bean
	public OAuthService oAuthService() {
		return new OAuthService(userDomainService, jwtUtil, objectMapper);
	}

	@Bean
	public CustomAuthenticationEntryPoint customAuthenticationEntryPoint() {
		return new CustomAuthenticationEntryPoint(objectMapper);
	}

	@Bean
	public JwtAuthenticationFilter jwtAuthenticationFilter() {
		return new JwtAuthenticationFilter(jwtUtil, customAuthenticationEntryPoint());
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
					"/auth/reissue",
					"/oauth2/**"
				).permitAll()
				.requestMatchers("/admin/**").hasRole("ADMIN") // 어드민 전용
				.requestMatchers(
					HttpMethod.GET,
					"/auctions/**",
					"/catalogs/**",
					"/products/**"
				).permitAll()
				.anyRequest().authenticated())
			.oauth2Login(oauth2 -> oauth2
				.userInfoEndpoint(userInfo -> userInfo
					.userService(oAuthService()))
				.successHandler(oAuth2SuccessHandler))
			.exceptionHandling(exception ->
				exception.authenticationEntryPoint(customAuthenticationEntryPoint()))
			.addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
			.build();
	}

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowedOrigins(List.of(
			"http://127.0.0.1:8080",    // 최종 배포 후 삭제
			"http://localhost:8080",    // 최종 배포 후 삭제
			"http://127.0.0.1:5173",    // 최종 배포 후 삭제
			"http://localhost:5173",    // 최종 배포 후 삭제
			"http://34.10.98.247:8080",    // 최종 배포 후 삭제
			"http://34.10.98.247:5173",    // 최종 배포 후 삭제
			"https://nebulazone-bz7n3o4r7-uguls-projects.vercel.app/",
			"https://nebulazone-fe.vercel.app/"
		));
		configuration.setAllowedMethods(List.of(
			"GET", "POST", "PUT", "DELETE", "OPTIONS"
		));
		configuration.setAllowedHeaders(List.of(
			"Authorization",
			"Content-Type",
			"X-Requested-With"
		));
		configuration.setExposedHeaders(List.of(
			"X-Total-Count"
		));
		configuration.setAllowCredentials(true);
		configuration.setMaxAge(3600L);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration auth) throws Exception {
		return auth.getAuthenticationManager();
	}
}
