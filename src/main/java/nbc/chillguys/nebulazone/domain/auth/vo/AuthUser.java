package nbc.chillguys.nebulazone.domain.auth.vo;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import lombok.Builder;
import lombok.Getter;
import nbc.chillguys.nebulazone.domain.user.dto.UserSignInInfo;
import nbc.chillguys.nebulazone.domain.user.entity.UserRole;

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

	public static AuthUser from(UserSignInInfo userInfo) {
		return AuthUser.builder()
			.id(userInfo.id())
			.email(userInfo.email())
			.roles(userInfo.roles())
			.build();
	}
}
