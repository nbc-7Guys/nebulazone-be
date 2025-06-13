package nbc.chillguys.nebulazone.domain.user.dto;

import java.util.Set;

import nbc.chillguys.nebulazone.domain.user.entity.UserRole;
import nbc.chillguys.nebulazone.domain.user.entity.UserStatus;

public record UserAdminSearchQueryCommand(
	String keyword,
	UserStatus userStatus,
	Set<UserRole> roles
) {
}
