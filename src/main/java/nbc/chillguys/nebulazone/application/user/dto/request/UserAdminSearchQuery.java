package nbc.chillguys.nebulazone.application.user.dto.request;

import java.util.Set;

import nbc.chillguys.nebulazone.domain.user.entity.UserRole;
import nbc.chillguys.nebulazone.domain.user.entity.UserStatus;

public record UserAdminSearchQuery(
	String keyword,
	UserStatus status,
	Set<UserRole> roles,
	int page,
	int size
) {
}
