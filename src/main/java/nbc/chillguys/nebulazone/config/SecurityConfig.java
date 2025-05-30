package nbc.chillguys.nebulazone.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.fasterxml.jackson.databind.ObjectMapper;

import nbc.chillguys.nebulazone.infra.security.jwt.JwtUtil;
import nbc.chillguys.nebulazone.infra.security.jwt.filter.CustomAuthenticationEntryPoint;
import nbc.chillguys.nebulazone.infra.security.jwt.filter.JwtAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
	private final CustomAuthenticationEntryPoint entryPoint;
	private final JwtAuthenticationFilter jwtAuthenticationFilter;

	public SecurityConfig(ObjectMapper objectMapper, JwtUtil jwtUtil) {
		this.entryPoint = new CustomAuthenticationEntryPoint(objectMapper);
		this.jwtAuthenticationFilter = new JwtAuthenticationFilter(jwtUtil, entryPoint);
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
					"/favicon.ico",
					"/error",
					"/actuator/**",
					"/metrics/**",
					"/api/v1/**"
				).permitAll()
				.requestMatchers(
					"/auth/signin",
					"/users/signup"
				).permitAll()
				.anyRequest().authenticated())
			.exceptionHandling(exception ->
				exception.authenticationEntryPoint(entryPoint))
			.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
			.build();
	}

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowedOrigins(List.of("*"));
		configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
		configuration.setAllowedHeaders(List.of("*"));
		configuration.setExposedHeaders(List.of("*"));
		configuration.setAllowCredentials(true);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration auth) throws Exception {
		return auth.getAuthenticationManager();
	}
}
