package nbc.chillguys.nebulazone.domain.user.entity;

import java.util.Arrays;

import nbc.chillguys.nebulazone.domain.user.exception.UserErrorCode;
import nbc.chillguys.nebulazone.domain.user.exception.UserException;

public enum UserRole {
	ROLE_USER, ROLE_ADMIN;

	public static UserRole from(String role) {
		return Arrays.stream(UserRole.values())
			.filter(r -> r.name().equalsIgnoreCase(role))
			.findFirst()
			.orElseThrow(() -> new UserException(UserErrorCode.WRONG_ROLES));
	}
}
