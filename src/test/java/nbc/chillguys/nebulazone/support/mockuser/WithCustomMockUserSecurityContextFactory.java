package nbc.chillguys.nebulazone.support.mockuser;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;
import org.springframework.test.util.ReflectionTestUtils;

import nbc.chillguys.nebulazone.domain.user.entity.Address;
import nbc.chillguys.nebulazone.domain.user.entity.OAuthType;
import nbc.chillguys.nebulazone.domain.user.entity.User;
import nbc.chillguys.nebulazone.domain.user.entity.UserRole;

public class WithCustomMockUserSecurityContextFactory implements WithSecurityContextFactory<WithCustomMockUser> {
	@Override
	public SecurityContext createSecurityContext(WithCustomMockUser annotation) {
		long id = annotation.id();
		String email = annotation.email();
		UserRole role = annotation.role();

		User user = User.builder()
			.email(email)
			.password("encodedPassword")
			.phone("01012345678")
			.nickname("test")
			.profileImage("test.jpg")
			.point(0)
			.oAuthType(OAuthType.DOMAIN)
			.roles(Set.of(role))
			.addresses(new ArrayList<>(List.of(Address.builder()
				.addressNickname("test_address_nickname")
				.roadAddress("test_road_address")
				.detailAddress("test_detail_address")
				.build())))
			.build();

		ReflectionTestUtils.setField(user, "id", id);

		UsernamePasswordAuthenticationToken token =
			new UsernamePasswordAuthenticationToken(user, null,
				List.of(new SimpleGrantedAuthority(annotation.role().name())));
		SecurityContext context = SecurityContextHolder.getContext();
		context.setAuthentication(token);
		return context;
	}
}
