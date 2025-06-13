package nbc.chillguys.nebulazone.domain.auth.vo;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import lombok.Builder;
import lombok.Getter;
import nbc.chillguys.nebulazone.domain.user.entity.User;
import nbc.chillguys.nebulazone.domain.user.entity.UserRole;
import nbc.chillguys.nebulazone.infra.oauth.dto.CustomOAuth2User;

@Getter
@Builder
public class AuthUser {
	private Long id;
	private String email;
	private Set<UserRole> roles;

	public Collection<? extends GrantedAuthority> getAuthorities() {
		return this.roles.stream()
			.map(role -> new SimpleGrantedAuthority(role.name()))
			.collect(Collectors.toSet());
	}

	public static AuthUser from(User user) {
		return AuthUser.builder()
			.id(user.getId())
			.email(user.getEmail())
			.roles(user.getRoles())
			.build();
	}

	public static AuthUser from(CustomOAuth2User oAuth2User) {
		return AuthUser.builder()
			.id(oAuth2User.userId())
			.email(oAuth2User.getName())
			.roles(oAuth2User.roles())
			.build();
	}
}
