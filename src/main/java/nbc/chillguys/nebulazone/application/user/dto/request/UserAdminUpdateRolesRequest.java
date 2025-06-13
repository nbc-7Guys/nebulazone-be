package nbc.chillguys.nebulazone.application.user.dto.request;

import java.util.Set;

import nbc.chillguys.nebulazone.domain.user.entity.UserRole;

public record UserAdminUpdateRolesRequest(
	Set<UserRole> roles
) {
}
