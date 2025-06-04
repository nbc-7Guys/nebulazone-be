package nbc.chillguys.nebulazone.support.mockuser;

import java.util.List;
import java.util.Set;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

import nbc.chillguys.nebulazone.domain.auth.vo.AuthUser;
import nbc.chillguys.nebulazone.domain.user.entity.UserRole;

public class WithCustomMockUserSecurityContextFactory implements WithSecurityContextFactory<WithCustomMockUser> {
	@Override
	public SecurityContext createSecurityContext(WithCustomMockUser annotation) {
		long id = annotation.id();
		String email = annotation.email();
		UserRole role = annotation.role();

		AuthUser user = AuthUser.builder()
			.id(id)
			.email(email)
			.roles(Set.of(role))
			.build();

		UsernamePasswordAuthenticationToken token =
			new UsernamePasswordAuthenticationToken(user, null,
				List.of(new SimpleGrantedAuthority(annotation.role().name())));
		SecurityContext context = SecurityContextHolder.getContext();
		context.setAuthentication(token);
		return context;
	}
}
