package nbc.chillguys.nebulazone.infra.websocket.dto;

import nbc.chillguys.nebulazone.domain.user.entity.User;

public record SessionUser(
	Long id,
	String email
) {
	public static SessionUser from(User user) {
		return new SessionUser(
			user.getId(),
			user.getEmail()
		);
	}
}
