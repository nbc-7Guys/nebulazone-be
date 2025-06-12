package nbc.chillguys.nebulazone.application.user.dto.request;

import nbc.chillguys.nebulazone.domain.user.entity.UserStatus;

public record AdminUserUpdateStatusRequest(
	UserStatus status
) {
}
