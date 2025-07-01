package nbc.chillguys.nebulazone.infra.security.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class JwtConstants {
	public static final String AUTH_HEADER = "Authorization";
	public static final String TOKEN_PREFIX = "Bearer ";
	public static final String KEY_ROLES = "roles";
}
