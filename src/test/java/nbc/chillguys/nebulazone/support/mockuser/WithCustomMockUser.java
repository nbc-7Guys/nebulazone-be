package nbc.chillguys.nebulazone.support.mockuser;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.springframework.security.test.context.support.WithSecurityContext;

import nbc.chillguys.nebulazone.domain.user.entity.UserRole;

@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithCustomMockUserSecurityContextFactory.class)
public @interface WithCustomMockUser {
	long id() default 1L;

	String email() default "test@test.com";

	UserRole role() default UserRole.ROLE_USER;
}
