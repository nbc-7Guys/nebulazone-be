package nbc.chillguys.nebulazone.domain.user.dto;

import java.util.Set;

import nbc.chillguys.nebulazone.domain.user.entity.User;
import nbc.chillguys.nebulazone.domain.user.entity.UserRole;

public record UserSignInInfo(
	Long id,
	String email,
	String password,
	Set<UserRole> roles
) {
	public static UserSignInInfo from(User user) {
		return new UserSignInInfo(
			user.getId(),
			user.getEmail(),
			user.getPassword(),
			user.getRoles()
		);
	}
}
