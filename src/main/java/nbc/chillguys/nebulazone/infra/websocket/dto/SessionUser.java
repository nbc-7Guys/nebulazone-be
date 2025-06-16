package nbc.chillguys.nebulazone.infra.websocket.dto;

import nbc.chillguys.nebulazone.domain.auth.vo.AuthUser;

public record SessionUser(
	Long id,
	String email
) {
	public static SessionUser from(AuthUser authUser) {
		return new SessionUser(
			authUser.getId(),
			authUser.getEmail()
		);
	}
}
