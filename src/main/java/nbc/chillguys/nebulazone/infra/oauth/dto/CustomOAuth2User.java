package nbc.chillguys.nebulazone.infra.oauth.dto;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import lombok.Builder;
import nbc.chillguys.nebulazone.domain.user.entity.UserRole;

@Builder
public record CustomOAuth2User(
	Set<UserRole> roles,
	Map<String, Object> attributes,
	String nameAttributeKey,
	Long userId,
	String accessToken,
	String refreshToken
) implements OAuth2User {
	@Override
	public Map<String, Object> getAttributes() {
		return attributes;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return this.roles.stream()
			.map(role -> new SimpleGrantedAuthority(role.name()))
			.collect(Collectors.toSet());
	}

	@Override
	public String getName() {
		return nameAttributeKey;
	}
}
